package io.dataease.rsa.dao.mapper;


import io.dataease.rsa.dao.entity.CoreRsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CoreRsaRepository extends JpaRepository<CoreRsa, Long>, JpaSpecificationExecutor<CoreRsa> {

}
