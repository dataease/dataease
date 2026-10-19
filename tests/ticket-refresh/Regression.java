package io.dataease.share.manage;

import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.exception.DEException;
import io.dataease.share.dao.auto.entity.CoreShareTicket;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.CoreShareTicketRepository;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/** Executes production rotation code against controlled repositories; no Spring/DB startup. */
public class Regression {
    private static CoreShareTicket stored;
    private static int writes;
    private static int assertions;
    private static boolean owner = true;
    private static boolean allowed = true;

    private static void check(boolean value) {
        assertions++;
        if (!value) throw new AssertionError("Assertion " + assertions);
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        var field = ShareTicketManage.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void reset(Long exp) {
        stored = new CoreShareTicket();
        stored.setId(1L);
        stored.setTicket("old");
        stored.setUuid("share");
        stored.setArgs("{\"region\":[\"saved\"]}");
        stored.setExp(exp);
        stored.setAccessTime(123L);
        writes = 0;
        owner = allowed = true;
    }

    private static TicketCreator request(String uuid, String ticket) {
        var request = new TicketCreator();
        request.setUuid(uuid);
        request.setTicket(ticket);
        request.setGenerateNew(true);
        request.setArgs("draft");
        request.setExp(999L);
        return request;
    }

    private static void denied(ShareTicketManage manage, TicketCreator request) {
        var before = stored;
        try { manage.saveTicket(request); throw new AssertionError("Expected rejection"); }
        catch (DEException expected) { check(writes == 0 && stored == before); }
    }

    public static void main(String[] args) throws Exception {
        new io.dataease.utils.IDUtils().setSnowFlake(new io.dataease.utils.SnowFlake(1, 1));
        var manage = new ShareTicketManage() {
            @Override public CoreShareTicket getByTicket(String ticket) {
                return stored != null && Objects.equals(stored.getTicket(), ticket) ? stored : null;
            }
        };
        var share = new XpackShare();
        share.setResourceId(3L);
        share.setUuid("share");
        inject(manage, "xpackShareRepository", Proxy.newProxyInstance(
                XpackShareRepository.class.getClassLoader(), new Class[]{XpackShareRepository.class},
                (proxy, method, values) -> {
                    if (method.getName().equals("findAll")) return owner ? List.of(share) : List.of();
                    throw new AssertionError(method);
                }));
        inject(manage, "shareAuthorizationManage", new ShareAuthorizationManage() {
            @Override public Long requireManage(Long resourceId) {
                if (!allowed) DEException.throwException("denied");
                return 1L;
            }
        });
        inject(manage, "coreShareTicketRepository", Proxy.newProxyInstance(
                CoreShareTicketRepository.class.getClassLoader(), new Class[]{CoreShareTicketRepository.class},
                (proxy, method, values) -> {
                    switch (method.getName()) {
                        case "deleteById": stored = null; writes++; return null;
                        case "saveAndFlush": stored = (CoreShareTicket) values[0]; writes++; return stored;
                        case "findById": return stored != null && Objects.equals(stored.getId(), values[0])
                                ? Optional.of(stored) : Optional.empty();
                        default: throw new AssertionError(method);
                    }
                }));
        for (Long exp : new Long[]{30L, 0L, null}) {
            reset(exp);
            String ticket = manage.saveTicket(request("share", "old"));
            check(!ticket.equals("old") && ticket.equals(stored.getTicket()));
            check(!stored.getId().equals(1L) && stored.getUuid().equals("share"));
            check(Objects.equals(exp, stored.getExp()));
            check(stored.getArgs().equals("{\"region\":[\"saved\"]}"));
            check(stored.getAccessTime() == null && writes == 2);
            check(!manage.validateTicket("old", share).isTicketValid());
            try { manage.verifyTokenTicket(1L, share); throw new AssertionError("Old token accepted"); }
            catch (IllegalArgumentException expected) { check(true); }
            manage.validateTicket(ticket, share);
            check(stored.getAccessTime() != null);
            check(manage.verifyTokenTicket(stored.getId(), share).id().equals(stored.getId()));
            writes = 0;
            denied(manage, request("share", "old"));
        }
        reset(30L); denied(manage, request(null, "old"));
        reset(30L); denied(manage, request("other-share", "old"));
        reset(30L); denied(manage, request("share", "missing"));
        reset(30L); denied(manage, request("share", ""));
        reset(30L); owner = false; denied(manage, request("share", "old"));
        reset(30L); allowed = false; denied(manage, request("share", "old"));
        reset(30L);
        var save = request("share", "old");
        save.setGenerateNew(false);
        check(manage.saveTicket(save).equals("old"));
        check(stored.getArgs().equals("draft") && stored.getExp().equals(999L));
        System.out.println(assertions + " assertions passed (controlled repositories, no transaction integration)");
    }
}
