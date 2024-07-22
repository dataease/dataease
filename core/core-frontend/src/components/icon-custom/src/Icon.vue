<script setup lang="ts">
import { computed } from 'vue'
import { propTypes } from '@/utils/propTypes'

import _401 from '@/assets/svg/401.svg'
import _403 from '@/assets/svg/403.svg'
import APIDs from '@/assets/svg/API-ds.svg'
import Apache_Hive from '@/assets/svg/Apache Hive.svg'
import Checkbox from '@/assets/svg/Checkbox.svg'
import DM from '@/assets/svg/DM.svg'
import DataEase from '@/assets/svg/DataEase.svg'
import Elasticsearch from '@/assets/svg/Elasticsearch.svg'
import ExcelDs from '@/assets/svg/Excel-ds.svg'
import Frame from '@/assets/svg/Frame.svg'
import KingBase from '@/assets/svg/KingBase.svg'
import Kylin from '@/assets/svg/Kylin.svg'
import Maxcompute from '@/assets/svg/Maxcompute.svg'
import PDF from '@/assets/svg/PDF.svg'
import Presto from '@/assets/svg/Presto.svg'
import StarRocksDs from '@/assets/svg/StarRocks-ds.svg'
import TiDBDs from '@/assets/svg/TiDB-ds.svg'
import activeBtn_copilot from '@/assets/svg/active-btn_copilot.svg'
import add from '@/assets/svg/add.svg'
import allMsg from '@/assets/svg/all-msg.svg'
import appearance from '@/assets/svg/appearance.svg'
import areaDark from '@/assets/svg/area-dark.svg'
import areaOrigin from '@/assets/svg/area-origin.svg'
import areaStackDark from '@/assets/svg/area-stack-dark.svg'
import areaStackOrigin from '@/assets/svg/area-stack-origin.svg'
import areaStack from '@/assets/svg/area-stack.svg'
import area from '@/assets/svg/area.svg'
import auth from '@/assets/svg/auth.svg'
import authentication from '@/assets/svg/authentication.svg'
import barDark from '@/assets/svg/bar-dark.svg'
import barGroupDark from '@/assets/svg/bar-group-dark.svg'
import barGroupOrigin from '@/assets/svg/bar-group-origin.svg'
import barGroupStackDark from '@/assets/svg/bar-group-stack-dark.svg'
import barGroupStackOrigin from '@/assets/svg/bar-group-stack-origin.svg'
import barGroupStack from '@/assets/svg/bar-group-stack.svg'
import barGroup from '@/assets/svg/bar-group.svg'
import barHorizontalDark from '@/assets/svg/bar-horizontal-dark.svg'
import barHorizontalOrigin from '@/assets/svg/bar-horizontal-origin.svg'
import barHorizontal from '@/assets/svg/bar-horizontal.svg'
import barOrigin from '@/assets/svg/bar-origin.svg'
import barRangeDark from '@/assets/svg/bar-range-dark.svg'
import barRangeOrigin from '@/assets/svg/bar-range-origin.svg'
import barRange from '@/assets/svg/bar-range.svg'
import barStackDark from '@/assets/svg/bar-stack-dark.svg'
import barStackHorizontalDark from '@/assets/svg/bar-stack-horizontal-dark.svg'
import barStackHorizontalOrigin from '@/assets/svg/bar-stack-horizontal-origin.svg'
import barStackHorizontal from '@/assets/svg/bar-stack-horizontal.svg'
import barStackOrigin from '@/assets/svg/bar-stack-origin.svg'
import barStack from '@/assets/svg/bar-stack.svg'
import bar from '@/assets/svg/bar.svg'
import bidirectionalBarDark from '@/assets/svg/bidirectional-bar-dark.svg'
import bidirectionalBarOrigin from '@/assets/svg/bidirectional-bar-origin.svg'
import bidirectionalBar from '@/assets/svg/bidirectional-bar.svg'
import board_1 from '@/assets/svg/board_1.svg'
import board_2 from '@/assets/svg/board_2.svg'
import board_3 from '@/assets/svg/board_3.svg'
import board_4 from '@/assets/svg/board_4.svg'
import board_5 from '@/assets/svg/board_5.svg'
import board_6 from '@/assets/svg/board_6.svg'
import board_7 from '@/assets/svg/board_7.svg'
import board_8 from '@/assets/svg/board_8.svg'
import board_9 from '@/assets/svg/board_9.svg'
import btn_copilot from '@/assets/svg/btn_copilot.svg'
import btn_oidc from '@/assets/svg/btn_oidc.svg'
import bubbleMapDark from '@/assets/svg/bubble-map-dark.svg'
import bubbleMapOrigin from '@/assets/svg/bubble-map-origin.svg'
import bubbleMap from '@/assets/svg/bubble-map.svg'
import button_right from '@/assets/svg/button_right.svg'
import cancel_release from '@/assets/svg/cancel_release.svg'
import chartDownload from '@/assets/svg/chart-download.svg'
import chartMixDark from '@/assets/svg/chart-mix-dark.svg'
import chartMixGroupDark from '@/assets/svg/chart-mix-group-dark.svg'
import chartMixGroupOrigin from '@/assets/svg/chart-mix-group-origin.svg'
import chartMixGroup from '@/assets/svg/chart-mix-group.svg'
import chartMixOrigin from '@/assets/svg/chart-mix-origin.svg'
import chartMixStackDark from '@/assets/svg/chart-mix-stack-dark.svg'
import chartMixStackOrigin from '@/assets/svg/chart-mix-stack-origin.svg'
import chartMixStack from '@/assets/svg/chart-mix-stack.svg'
import chartMix from '@/assets/svg/chart-mix.svg'
import chartTable from '@/assets/svg/chart-table.svg'
import ckDs from '@/assets/svg/ck-ds.svg'
import copilot from '@/assets/svg/copilot.svg'
import custom_sort from '@/assets/svg/custom_sort.svg'
import dark_1 from '@/assets/svg/dark_1.svg'
import dashboard from '@/assets/svg/dashboard.svg'
import dataReference from '@/assets/svg/data-reference.svg'
import database from '@/assets/svg/database.svg'
import datasetOutline from '@/assets/svg/dataset-outline.svg'
import datasetTask from '@/assets/svg/dataset-task.svg'
import datasource from '@/assets/svg/datasource.svg'
import dbDe from '@/assets/svg/db-de.svg'
import dbMoreWeb from '@/assets/svg/db-more-web.svg'
import db2Ds from '@/assets/svg/db2-ds.svg'
import deApiNew from '@/assets/svg/de-api-new.svg'
import deCopy from '@/assets/svg/de-copy.svg'
import deDbNew from '@/assets/svg/de-db-new.svg'
import deDelete from '@/assets/svg/de-delete.svg'
import deDsError from '@/assets/svg/de-ds-error.svg'
import deDsMove from '@/assets/svg/de-ds-move.svg'
import deDsRename from '@/assets/svg/de-ds-rename.svg'
import deDsTrash from '@/assets/svg/de-ds-trash.svg'
import deDsWarning from '@/assets/svg/de-ds-warning.svg'
import deExcelNew from '@/assets/svg/de-excel-new.svg'
import deJson from '@/assets/svg/de-json.svg'
import deMove from '@/assets/svg/de-move.svg'
import deSearch from '@/assets/svg/de-search.svg'
import deSqlNew from '@/assets/svg/de-sql-new.svg'
import deUnionNew from '@/assets/svg/de-union-new.svg'
import de_pwd_invisible from '@/assets/svg/de_pwd_invisible.svg'
import de_pwd_visible from '@/assets/svg/de_pwd_visible.svg'
import default_avatar from '@/assets/svg/default_avatar.svg'
import _delete from '@/assets/svg/delete.svg'
import displaySetting from '@/assets/svg/display-setting.svg'
import doc from '@/assets/svg/doc.svg'
import docs from '@/assets/svg/docs.svg'
import dorisDs from '@/assets/svg/doris-ds.svg'
import drag from '@/assets/svg/drag.svg'
import driverDe from '@/assets/svg/driver-de.svg'
import dsApi from '@/assets/svg/ds-api.svg'
import dsCustom from '@/assets/svg/ds-custom.svg'
import dsDb from '@/assets/svg/ds-db.svg'
import dsExcel from '@/assets/svg/ds-excel.svg'
import dsSql from '@/assets/svg/ds-sql.svg'
import dsUnion from '@/assets/svg/ds-union.svg'
import dvAiWindowMax from '@/assets/svg/dv-ai-window-max.svg'
import dvAiWindowMin from '@/assets/svg/dv-ai-window-min.svg'
import dvAi from '@/assets/svg/dv-ai.svg'
import dvBarEnlarge from '@/assets/svg/dv-bar-enlarge.svg'
import dvBarUnLinkage from '@/assets/svg/dv-bar-unLinkage.svg'
import dvBatch from '@/assets/svg/dv-batch.svg'
import dvCopyDark from '@/assets/svg/dv-copy-dark.svg'
import dvCopy from '@/assets/svg/dv-copy.svg'
import dvDashboardSpineMobile from '@/assets/svg/dv-dashboard-spine-mobile.svg'
import dvDashboardSpine from '@/assets/svg/dv-dashboard-spine.svg'
import dvDashboard from '@/assets/svg/dv-dashboard.svg'
import dvDelete from '@/assets/svg/dv-delete.svg'
import dvDetails from '@/assets/svg/dv-details.svg'
import dvDragTips from '@/assets/svg/dv-drag-tips.svg'
import dvEdit from '@/assets/svg/dv-edit.svg'
import dvEmpty from '@/assets/svg/dv-empty.svg'
import dvExpandDown from '@/assets/svg/dv-expand-down.svg'
import dvExpandRight from '@/assets/svg/dv-expand-right.svg'
import dvEyeClose from '@/assets/svg/dv-eye-close.svg'
import dvFilterShow from '@/assets/svg/dv-filter-show.svg'
import dvFilter from '@/assets/svg/dv-filter.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import dvHeadMore from '@/assets/svg/dv-head-more.svg'
import dvInfo from '@/assets/svg/dv-info.svg'
import dvLinkTarget from '@/assets/svg/dv-link-target.svg'
import dvLock from '@/assets/svg/dv-lock.svg'
import dvMaterial from '@/assets/svg/dv-material.svg'
import dvMax from '@/assets/svg/dv-max.svg'
import dvMedia from '@/assets/svg/dv-media.svg'
import dvMin from '@/assets/svg/dv-min.svg'
import dvMoreCom from '@/assets/svg/dv-more-com.svg'
import dvMoreTimeClock from '@/assets/svg/dv-more-time-clock.svg'
import dvMore from '@/assets/svg/dv-more.svg'
import dvMove from '@/assets/svg/dv-move.svg'
import dvNewFolder from '@/assets/svg/dv-new-folder.svg'
import dvNew from '@/assets/svg/dv-new.svg'
import dvNoImg from '@/assets/svg/dv-no-img.svg'
import dvNothing from '@/assets/svg/dv-nothing.svg'
import dvParams from '@/assets/svg/dv-params.svg'
import dvPictureReal from '@/assets/svg/dv-picture-real.svg'
import dvPictureShow from '@/assets/svg/dv-picture-show.svg'
import dvPicture from '@/assets/svg/dv-picture.svg'
import dvPreviewDownload from '@/assets/svg/dv-preview-download.svg'
import dvPreviewInner from '@/assets/svg/dv-preview-inner.svg'
import dvPreviewOuter from '@/assets/svg/dv-preview-outer.svg'
import dvPreview from '@/assets/svg/dv-preview.svg'
import dvRename from '@/assets/svg/dv-rename.svg'
import dvReposition from '@/assets/svg/dv-reposition.svg'
import dvRichText from '@/assets/svg/dv-richText.svg'
import dvRuler from '@/assets/svg/dv-ruler.svg'
import dvScreenNew from '@/assets/svg/dv-screen-new.svg'
import dvScreenSpine from '@/assets/svg/dv-screen-spine.svg'
import dvScrollText from '@/assets/svg/dv-scroll-text.svg'
import dvShare from '@/assets/svg/dv-share.svg'
import dvShow from '@/assets/svg/dv-show.svg'
import dvSortAsc from '@/assets/svg/dv-sort-asc.svg'
import dvSortDesc from '@/assets/svg/dv-sort-desc.svg'
import dvStyleActiveFont from '@/assets/svg/dv-style-activeFont.svg'
import dvStyleActiveFontSize from '@/assets/svg/dv-style-activeFontSize.svg'
import dvStyleBackgroundColor from '@/assets/svg/dv-style-backgroundColor.svg'
import dvStyleBorderColor from '@/assets/svg/dv-style-borderColor.svg'
import dvStyleBorderRadius from '@/assets/svg/dv-style-borderRadius.svg'
import dvStyleBorderSize from '@/assets/svg/dv-style-borderSize.svg'
import dvStyleBorderStyle from '@/assets/svg/dv-style-borderStyle.svg'
import dvStyleColor from '@/assets/svg/dv-style-color.svg'
import dvStyleFontFamily from '@/assets/svg/dv-style-fontFamily.svg'
import dvStyleFontSize from '@/assets/svg/dv-style-fontSize.svg'
import dvStyleFontWeight from '@/assets/svg/dv-style-fontWeight.svg'
import dvStyleHeadBorderActiveColor from '@/assets/svg/dv-style-headBorderActiveColor.svg'
import dvStyleHeadBorderColor from '@/assets/svg/dv-style-headBorderColor.svg'
import dvStyleHeadFontActiveColor from '@/assets/svg/dv-style-headFontActiveColor.svg'
import dvStyleHeadFontColor from '@/assets/svg/dv-style-headFontColor.svg'
import dvStyleHeadHorizontalPosition from '@/assets/svg/dv-style-headHorizontalPosition.svg'
import dvStyleLetterSpacing from '@/assets/svg/dv-style-letterSpacing.svg'
import dvStyleLineHeight from '@/assets/svg/dv-style-lineHeight.svg'
import dvStyleOpacity from '@/assets/svg/dv-style-opacity.svg'
import dvStyleScrollSpeed from '@/assets/svg/dv-style-scroll-speed.svg'
import dvStyleTextAlign from '@/assets/svg/dv-style-textAlign.svg'
import dvTabShow from '@/assets/svg/dv-tab-show.svg'
import dvTab from '@/assets/svg/dv-tab.svg'
import dvText from '@/assets/svg/dv-text.svg'
import dvUnlock from '@/assets/svg/dv-unlock.svg'
import dvUpArrow from '@/assets/svg/dv-up-arrow.svg'
import dvUseTemplate from '@/assets/svg/dv-use-template.svg'
import dvVideo from '@/assets/svg/dv-video.svg'
import dvView from '@/assets/svg/dv-view.svg'
import dv_mobile_layout from '@/assets/svg/dv_mobile_layout.svg'
import editDone from '@/assets/svg/edit-done.svg'
import editIn from '@/assets/svg/edit-in.svg'
import edit from '@/assets/svg/edit.svg'
import emailTask from '@/assets/svg/email-task.svg'
import embedded from '@/assets/svg/embedded.svg'
import example from '@/assets/svg/example.svg'
import exclamationmark from '@/assets/svg/exclamationmark.svg'
import exclamationmark2 from '@/assets/svg/exclamationmark2.svg'
import exitFullscreen from '@/assets/svg/exit-fullscreen.svg'
import eyeOpen from '@/assets/svg/eye-open.svg'
import eye from '@/assets/svg/eye.svg'
import field_location from '@/assets/svg/field_location.svg'
import field_text from '@/assets/svg/field_text.svg'
import field_time from '@/assets/svg/field_time.svg'
import field_value from '@/assets/svg/field_value.svg'
import filterCenter from '@/assets/svg/filter-center.svg'
import filterHCenter from '@/assets/svg/filter-h-center.svg'
import filterHLeft from '@/assets/svg/filter-h-left.svg'
import filterHRight from '@/assets/svg/filter-h-right.svg'
import filterTop from '@/assets/svg/filter-top.svg'
import filter from '@/assets/svg/filter.svg'
import flowMapDark from '@/assets/svg/flow-map-dark.svg'
import flowMapOrigin from '@/assets/svg/flow-map-origin.svg'
import flowMap from '@/assets/svg/flow-map.svg'
import folder from '@/assets/svg/folder.svg'
import form from '@/assets/svg/form.svg'
import fullscreen from '@/assets/svg/fullscreen.svg'
import funnelDark from '@/assets/svg/funnel-dark.svg'
import funnelOrigin from '@/assets/svg/funnel-origin.svg'
import funnel from '@/assets/svg/funnel.svg'
import gaugeDark from '@/assets/svg/gauge-dark.svg'
import gaugeOrigin from '@/assets/svg/gauge-origin.svg'
import gauge from '@/assets/svg/gauge.svg'
import graphicalCircular from '@/assets/svg/graphical-circular.svg'
import graphicalRect from '@/assets/svg/graphical-rect.svg'
import graphicalTriangle from '@/assets/svg/graphical-triangle.svg'
import group3400 from '@/assets/svg/group-3400.svg'
import group from '@/assets/svg/group.svg'
import heatMapDark from '@/assets/svg/heat-map-dark.svg'
import heatMapOrigin from '@/assets/svg/heat-map-origin.svg'
import heatMap from '@/assets/svg/heat-map.svg'
import iconCtrlClose from '@/assets/svg/icon/ctrl/close.svg'
import iconOutlineNotification from '@/assets/svg/icon/outline/notification.svg'
import iconAlarmclock from '@/assets/svg/icon-alarmclock.svg'
import iconContacts from '@/assets/svg/icon-contacts.svg'
import iconDraft from '@/assets/svg/icon-draft.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import iconGroup from '@/assets/svg/icon-group.svg'
import iconImageUpload from '@/assets/svg/icon-image-upload.svg'
import iconImage from '@/assets/svg/icon-image.svg'
import iconLaser from '@/assets/svg/icon-laser.svg'
import iconLock from '@/assets/svg/icon-lock.svg'
import iconMaybe from '@/assets/svg/icon-maybe.svg'
import iconMaybe_outlined from '@/assets/svg/icon-maybe_outlined.svg'
import iconMore from '@/assets/svg/icon-more.svg'
import iconQuicksetting from '@/assets/svg/icon-quicksetting.svg'
import iconSetting from '@/assets/svg/icon-setting.svg'
import iconStream from '@/assets/svg/icon-stream.svg'
import iconVideo from '@/assets/svg/icon-video.svg'
import icon_Batch_outlined from '@/assets/svg/icon_Batch_outlined.svg'
import icon_Invalid_colorful from '@/assets/svg/icon_Invalid_colorful.svg'
import icon_addDictionary_outlined from '@/assets/svg/icon_add-dictionary_outlined.svg'
import icon_addEntry_outlined from '@/assets/svg/icon_add-entry_outlined.svg'
import icon_addFolder_outlined from '@/assets/svg/icon_add-folder_outlined.svg'
import icon_add_outlined1 from '@/assets/svg/icon_add_outlined-1.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_admin_outlined from '@/assets/svg/icon_admin_outlined.svg'
import icon_apiOutlined from '@/assets/svg/icon_api-outlined.svg'
import icon_api from '@/assets/svg/icon_api.svg'
import icon_app_outlined from '@/assets/svg/icon_app_outlined.svg'
import icon_arrowRight_outlined from '@/assets/svg/icon_arrow-right_outlined.svg'
import icon_assigned_outlined from '@/assets/svg/icon_assigned_outlined.svg'
import icon_attachment_outlined from '@/assets/svg/icon_attachment_outlined.svg'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import icon_bottomAlign_outlined from '@/assets/svg/icon_bottom-align_outlined.svg'
import icon_calendar_outlined from '@/assets/svg/icon_calendar_outlined.svg'
import icon_cancel_store from '@/assets/svg/icon_cancel_store.svg'
import icon_card_outlined from '@/assets/svg/icon_card_outlined.svg'
import icon_centerAlignment_outlined from '@/assets/svg/icon_center-alignment_outlined.svg'
import icon_chartLineC from '@/assets/svg/icon_chart-line-c.svg'
import icon_chartLine from '@/assets/svg/icon_chart-line.svg'
import icon_clear_outlined from '@/assets/svg/icon_clear_outlined.svg'
import icon_close_filled from '@/assets/svg/icon_close_filled.svg'
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import icon_collect_filled from '@/assets/svg/icon_collect_filled.svg'
import icon_collection_outlined from '@/assets/svg/icon_collection_outlined.svg'
import icon_copy_filled from '@/assets/svg/icon_copy_filled.svg'
import icon_copy_outlined from '@/assets/svg/icon_copy_outlined.svg'
import icon_customize from '@/assets/svg/icon_customize.svg'
import icon_dashboard from '@/assets/svg/icon_dashboard.svg'
import icon_dashboard_outlinedC from '@/assets/svg/icon_dashboard_outlined-c.svg'
import icon_dashboard_outlined from '@/assets/svg/icon_dashboard_outlined.svg'
import icon_databaseAlert_filled from '@/assets/svg/icon_database-alert_filled.svg'
import icon_databaseFail_filled from '@/assets/svg/icon_database-fail_filled.svg'
import icon_database from '@/assets/svg/icon_database.svg'
import icon_database_outlined from '@/assets/svg/icon_database_outlined.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import icon_dataset_outlined from '@/assets/svg/icon_dataset_outlined.svg'
import icon_db_filled from '@/assets/svg/icon_db_filled.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_describe_outlined from '@/assets/svg/icon_describe_outlined.svg'
import icon_dialpad_outlined from '@/assets/svg/icon_dialpad_outlined.svg'
import icon_disordeList_outlined from '@/assets/svg/icon_disorde-list_outlined.svg'
import icon_divider_outlined from '@/assets/svg/icon_divider_outlined.svg'
import icon_docReplace_outlined from '@/assets/svg/icon_doc-replace_outlined.svg'
import icon_done_outlined from '@/assets/svg/icon_done_outlined.svg'
import icon_downRight_outlined from '@/assets/svg/icon_down-right_outlined.svg'
import icon_down_outlined1 from '@/assets/svg/icon_down_outlined-1.svg'
import icon_down_outlined from '@/assets/svg/icon_down_outlined.svg'
import icon_download_outlined from '@/assets/svg/icon_download_outlined.svg'
import icon_drag_outlined from '@/assets/svg/icon_drag_outlined.svg'
import icon_drilling_outlined from '@/assets/svg/icon_drilling_outlined.svg'
import icon_drive_filled from '@/assets/svg/icon_drive_filled.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_effects_outlined from '@/assets/svg/icon_effects_outlined.svg'
import icon_excel from '@/assets/svg/icon_excel.svg'
import icon_excel_outlined from '@/assets/svg/icon_excel_outlined.svg'
import icon_expandDown_filled from '@/assets/svg/icon_expand-down_filled.svg'
import icon_expandLeft_filled from '@/assets/svg/icon_expand-left_filled.svg'
import icon_expandRight_filled from '@/assets/svg/icon_expand-right_filled.svg'
import icon_fileAdd_outlined from '@/assets/svg/icon_file-add_outlined.svg'
import icon_fileExcel_colorful from '@/assets/svg/icon_file-excel_colorful.svg'
import icon_folder_filled from '@/assets/svg/icon_folder_filled.svg'
import icon_fontColor_outlined from '@/assets/svg/icon_font-color_outlined.svg'
import icon_form_outlined from '@/assets/svg/icon_form_outlined.svg'
import icon_fullAssociation from '@/assets/svg/icon_full-association.svg'
import icon_functions_outlined from '@/assets/svg/icon_functions_outlined.svg'
import icon_graphical from '@/assets/svg/icon_graphical.svg'
import icon_gridlines_outlined from '@/assets/svg/icon_gridlines_outlined.svg'
import icon_h1_outlined from '@/assets/svg/icon_h1_outlined.svg'
import icon_h2_outlined from '@/assets/svg/icon_h2_outlined.svg'
import icon_h3_outlined from '@/assets/svg/icon_h3_outlined.svg'
import icon_hn_outlined from '@/assets/svg/icon_hn_outlined.svg'
import icon_horizontalAlign_outlined from '@/assets/svg/icon_horizontal-align_outlined.svg'
import icon_info_colorful from '@/assets/svg/icon_info_colorful.svg'
import icon_info_filled from '@/assets/svg/icon_info_filled.svg'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_intersect from '@/assets/svg/icon_intersect.svg'
import icon_intoItem_outlined from '@/assets/svg/icon_into-item_outlined.svg'
import icon_invisible_outlined from '@/assets/svg/icon_invisible_outlined.svg'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import icon_leftAlign_outlined from '@/assets/svg/icon_left-align_outlined.svg'
import icon_leftAlignment_outlined from '@/assets/svg/icon_left-alignment_outlined.svg'
import icon_leftAssociation from '@/assets/svg/icon_left-association.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_letterSpacing_outlined from '@/assets/svg/icon_letter-spacing_outlined.svg'
import icon_linkRecord from '@/assets/svg/icon_link-record.svg'
import icon_linkRecord_outlined1 from '@/assets/svg/icon_link-record_outlined-1.svg'
import icon_linkRecord_outlined from '@/assets/svg/icon_link-record_outlined.svg'
import icon_loading_outlined from '@/assets/svg/icon_loading_outlined.svg'
import icon_local from '@/assets/svg/icon_local.svg'
import icon_local_outlined from '@/assets/svg/icon_local_outlined.svg'
import icon_logs_outlined1 from '@/assets/svg/icon_logs_outlined-1.svg'
import icon_logs_outlined from '@/assets/svg/icon_logs_outlined.svg'
import icon_magnify_outlined from '@/assets/svg/icon_magnify_outlined.svg'
import icon_memberAdd_outlined from '@/assets/svg/icon_member-add_outlined.svg'
import icon_member_filled from '@/assets/svg/icon_member_filled.svg'
import icon_minify_outlined from '@/assets/svg/icon_minify_outlined.svg'
import icon_momentsCategories_outlined from '@/assets/svg/icon_moments-categories_outlined.svg'
import icon_moreVertical_outlined from '@/assets/svg/icon_more-vertical_outlined.svg'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import icon_newItem_outlined from '@/assets/svg/icon_new-item_outlined.svg'
import icon_number_outlined from '@/assets/svg/icon_number_outlined.svg'
import icon_operationAnalysis_outlined from '@/assets/svg/icon_operation-analysis_outlined.svg'
import icon_ordeList_outlined from '@/assets/svg/icon_orde-list_outlined.svg'
import icon_organization_outlined from '@/assets/svg/icon_organization_outlined.svg'
import icon_params_setting from '@/assets/svg/icon_params_setting.svg'
import icon_pc_fullscreen from '@/assets/svg/icon_pc_fullscreen.svg'
import icon_pc_outlined from '@/assets/svg/icon_pc_outlined.svg'
import icon_pc_outlined_copy from '@/assets/svg/icon_pc_outlined_copy.svg'
import icon_phone_outlined from '@/assets/svg/icon_phone_outlined.svg'
import icon_pie_outlinedC from '@/assets/svg/icon_pie_outlined-c.svg'
import icon_playRound_filled from '@/assets/svg/icon_play-round_filled.svg'
import icon_playRound_outlined from '@/assets/svg/icon_play-round_outlined.svg'
import icon_plugin_outlined from '@/assets/svg/icon_plugin_outlined.svg'
import icon_pullLeft_outlined from '@/assets/svg/icon_pull-left_outlined.svg'
import icon_pullRight_outlined from '@/assets/svg/icon_pull-right_outlined.svg'
import icon_qr_outlined from '@/assets/svg/icon_qr_outlined.svg'
import icon_redo_outlined from '@/assets/svg/icon_redo_outlined.svg'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import icon_replace_outlined from '@/assets/svg/icon_replace_outlined.svg'
import icon_reset_outlined from '@/assets/svg/icon_reset_outlined.svg'
import icon_resetpassword from '@/assets/svg/icon_resetpassword.svg'
import icon_rightAlign_outlined from '@/assets/svg/icon_right-align_outlined.svg'
import icon_rightAlignment_outlined from '@/assets/svg/icon_right-alignment_outlined.svg'
import icon_rightAssociation from '@/assets/svg/icon_right-association.svg'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import icon_scroll_filled from '@/assets/svg/icon_scroll_filled.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_search from '@/assets/svg/icon_search.svg'
import icon_shareLabel_filled from '@/assets/svg/icon_share-label_filled.svg'
import icon_shareLabel_outlined from '@/assets/svg/icon_share-label_outlined.svg'
import icon_sideExpand_outlined from '@/assets/svg/icon_side-expand_outlined.svg'
import icon_sideFold_outlined from '@/assets/svg/icon_side-fold_outlined.svg'
import icon_sortAToZ_outlined from '@/assets/svg/icon_sort-a-to-z_outlined.svg'
import icon_sortZToA_outlined from '@/assets/svg/icon_sort-z-to-a_outlined.svg'
import icon_sort_outlined from '@/assets/svg/icon_sort_outlined.svg'
import icon_sql from '@/assets/svg/icon_sql.svg'
import icon_sql_outlined from '@/assets/svg/icon_sql_outlined.svg'
import icon_sql_outlined_1 from '@/assets/svg/icon_sql_outlined_1.svg'
import icon_stretch_outlined from '@/assets/svg/icon_stretch_outlined.svg'
import icon_styleSet_outlined from '@/assets/svg/icon_style-set_outlined.svg'
import icon_succeed_colorful from '@/assets/svg/icon_succeed_colorful.svg'
import icon_succeed_filled from '@/assets/svg/icon_succeed_filled.svg'
import icon_switch_outlined from '@/assets/svg/icon_switch_outlined.svg'
import icon_syncPlayRound_filled from '@/assets/svg/icon_sync-play-round_filled.svg'
import icon_syncPlayRound_outlined from '@/assets/svg/icon_sync-play-round_outlined.svg'
import icon_sync_close_log_details from '@/assets/svg/icon_sync_close_log_details.svg'
import icon_sync_datasource from '@/assets/svg/icon_sync_datasource.svg'
import icon_sync_log_number from '@/assets/svg/icon_sync_log_number.svg'
import icon_sync_logs_outlined from '@/assets/svg/icon_sync_logs_outlined.svg'
import icon_sync_progress from '@/assets/svg/icon_sync_progress.svg'
import icon_sync_target_to_datasource from '@/assets/svg/icon_sync_target_to_datasource.svg'
import icon_sync_task_number from '@/assets/svg/icon_sync_task_number.svg'
import icon_takeAction_outlined from '@/assets/svg/icon_take-action_outlined.svg'
import icon_teamAdd_outlined from '@/assets/svg/icon_team-add_outlined.svg'
import icon_template from '@/assets/svg/icon_template.svg'
import icon_template_colorful from '@/assets/svg/icon_template_colorful.svg'
import icon_template_outlined from '@/assets/svg/icon_template_outlined.svg'
import icon_textBox_outlined from '@/assets/svg/icon_text-box_outlined.svg'
import icon_text_outlined from '@/assets/svg/icon_text_outlined.svg'
import icon_time_outlined from '@/assets/svg/icon_time_outlined.svg'
import icon_titleLeftAlign_outlined from '@/assets/svg/icon_title-left-align_outlined.svg'
import icon_titleTopAlign_outlined from '@/assets/svg/icon_title-top-align_outlined.svg'
import icon_topAlign_outlined from '@/assets/svg/icon_top-align_outlined.svg'
import icon_undo_outlined from '@/assets/svg/icon_undo_outlined.svg'
import icon_upLeft_outlined from '@/assets/svg/icon_up-left_outlined.svg'
import icon_upload_outlined from '@/assets/svg/icon_upload_outlined.svg'
import icon_verticalAlign_outlined from '@/assets/svg/icon_vertical-align_outlined.svg'
import icon_viewList_outlined from '@/assets/svg/icon_view-list_outlined.svg'
import icon_viewinchat_outlined from '@/assets/svg/icon_viewinchat_outlined.svg'
import icon_visible_outlined from '@/assets/svg/icon_visible_outlined.svg'
import icon_warning_colorful from '@/assets/svg/icon_warning_colorful.svg'
import icon_warning_filled from '@/assets/svg/icon_warning_filled.svg'
import icon_yes_outlined from '@/assets/svg/icon_yes_outlined.svg'
import iconfont from '@/assets/svg/iconfont.svg'
import impalaDs from '@/assets/svg/impala-ds.svg'
import indicatorDark from '@/assets/svg/indicator-dark.svg'
import indicatorOrigin from '@/assets/svg/indicator-origin.svg'
import indicator from '@/assets/svg/indicator.svg'
import innerJoin from '@/assets/svg/inner-join.svg'
import international from '@/assets/svg/international.svg'
import joinJoin from '@/assets/svg/join-join.svg'
import label from '@/assets/svg/label.svg'
import language from '@/assets/svg/language.svg'
import leftJoin from '@/assets/svg/left-join.svg'
import lineDark from '@/assets/svg/line-dark.svg'
import lineOrigin from '@/assets/svg/line-origin.svg'
import line from '@/assets/svg/line.svg'
import linkBack from '@/assets/svg/link-back.svg'
import linkDown from '@/assets/svg/link-down.svg'
import link from '@/assets/svg/link.svg'
import liquidDark from '@/assets/svg/liquid-dark.svg'
import liquidOrigin from '@/assets/svg/liquid-origin.svg'
import liquid from '@/assets/svg/liquid.svg'
import lock_closed from '@/assets/svg/lock_closed.svg'
import lock_closed_switch from '@/assets/svg/lock_closed_switch.svg'
import lock_open from '@/assets/svg/lock_open.svg'
import lock_open_back from '@/assets/svg/lock_open_back.svg'
import lock_other_open from '@/assets/svg/lock_other_open.svg'
import log from '@/assets/svg/log.svg'
import logo from '@/assets/svg/logo.svg'
import logo_cas from '@/assets/svg/logo_cas.svg'
import logo_ldap from '@/assets/svg/logo_ldap.svg'
import logo_dingtalk from '@/assets/svg/logo_dingtalk.svg'
import logo_lark from '@/assets/svg/logo_lark.svg'
import logo_oauth from '@/assets/svg/logo_oauth.svg'
import logo_wechatWork from '@/assets/svg/logo_wechat-work.svg'
import mapDark from '@/assets/svg/map-dark.svg'
import mapOrigin from '@/assets/svg/map-origin.svg'
import map from '@/assets/svg/map.svg'
import map_mini from '@/assets/svg/map_mini.svg'
import map_old from '@/assets/svg/map_old.svg'
import mariadbDs from '@/assets/svg/mariadb-ds.svg'
import marketExpand from '@/assets/svg/market-expand.svg'
import marketRetract from '@/assets/svg/market-retract.svg'
import menu from '@/assets/svg/menu.svg'
import mobileIcon_dashboard_filled from '@/assets/svg/mobile/icon_dashboard_filled.svg'
import mobileIcon_dashboard_outlined from '@/assets/svg/mobile/icon_dashboard_outlined.svg'
import mobileIcon_home_filled from '@/assets/svg/mobile/icon_home_filled.svg'
import mobileIcon_home_outlined from '@/assets/svg/mobile/icon_home_outlined.svg'
import mobileIcon_member_filled from '@/assets/svg/mobile/icon_member_filled.svg'
import mobileIcon_member_outlined from '@/assets/svg/mobile/icon_member_outlined.svg'
import mobileCheckbox from '@/assets/svg/mobile-checkbox.svg'
import mongoDs from '@/assets/svg/mongo-ds.svg'
import more_v from '@/assets/svg/more_v.svg'
import move from '@/assets/svg/move.svg'
import msgSetting from '@/assets/svg/msg-setting.svg'
import mysqlDs from '@/assets/svg/mysql-ds.svg'
import mysqlFrame from '@/assets/svg/mysql-frame.svg'
import nested from '@/assets/svg/nested.svg'
import noJoin from '@/assets/svg/no-join.svg'
import no_result from '@/assets/svg/no_result.svg'
import nonExistent from '@/assets/svg/non-existent.svg'
import none_select from '@/assets/svg/none_select.svg'
import notification from '@/assets/svg/notification.svg'
import oracleDs from '@/assets/svg/oracle-ds.svg'
import org from '@/assets/svg/org.svg'
import other_material from '@/assets/svg/other_material.svg'
import other_material_board from '@/assets/svg/other_material_board.svg'
import other_material_icon from '@/assets/svg/other_material_icon.svg'
import other_media from '@/assets/svg/other_media.svg'
import other_setting from '@/assets/svg/other_setting.svg'
import other_text from '@/assets/svg/other_text.svg'
import pageLine from '@/assets/svg/page-line.svg'
import panelMobilePublish from '@/assets/svg/panel-mobile-publish.svg'
import panelMobileUnpublishedBack from '@/assets/svg/panel-mobile-unpublished-back.svg'
import panelMobileUnpublished from '@/assets/svg/panel-mobile-unpublished.svg'
import panelPublish from '@/assets/svg/panel-publish.svg'
import panelUnpublishedBack from '@/assets/svg/panel-unpublished-back.svg'
import panelUnpublished from '@/assets/svg/panel-unpublished.svg'
import panel from '@/assets/svg/panel.svg'
import people from '@/assets/svg/people.svg'
import peoples from '@/assets/svg/peoples.svg'
import percentageBarStackDark from '@/assets/svg/percentage-bar-stack-dark.svg'
import percentageBarStackHorizontalDark from '@/assets/svg/percentage-bar-stack-horizontal-dark.svg'
import percentageBarStackHorizontalOrigin from '@/assets/svg/percentage-bar-stack-horizontal-origin.svg'
import percentageBarStackHorizontal from '@/assets/svg/percentage-bar-stack-horizontal.svg'
import percentageBarStackOrigin from '@/assets/svg/percentage-bar-stack-origin.svg'
import percentageBarStack from '@/assets/svg/percentage-bar-stack.svg'
import pgDs from '@/assets/svg/pg-ds.svg'
import pieDark from '@/assets/svg/pie-dark.svg'
import pieDonutDark from '@/assets/svg/pie-donut-dark.svg'
import pieDonutOrigin from '@/assets/svg/pie-donut-origin.svg'
import pieDonutRoseDark from '@/assets/svg/pie-donut-rose-dark.svg'
import pieDonutRoseOrigin from '@/assets/svg/pie-donut-rose-origin.svg'
import pieDonutRose from '@/assets/svg/pie-donut-rose.svg'
import pieDonut from '@/assets/svg/pie-donut.svg'
import pieOrigin from '@/assets/svg/pie-origin.svg'
import pieRoseDark from '@/assets/svg/pie-rose-dark.svg'
import pieRoseOrigin from '@/assets/svg/pie-rose-origin.svg'
import pieRose from '@/assets/svg/pie-rose.svg'
import pie from '@/assets/svg/pie.svg'
import platform from '@/assets/svg/platform.svg'
import plugin3dmap from '@/assets/svg/plugin-3dmap.svg'
import plugin3dpie from '@/assets/svg/plugin-3dpie.svg'
import pluginDameng from '@/assets/svg/plugin-dameng.svg'
import pluginDefault from '@/assets/svg/plugin-default.svg'
import plugin from '@/assets/svg/plugin.svg'
import pluginsNew from '@/assets/svg/plugins-new.svg'
import plugins from '@/assets/svg/plugins.svg'
import progressBarDark from '@/assets/svg/progress-bar-dark.svg'
import progressBarOrigin from '@/assets/svg/progress-bar-origin.svg'
import progressBar from '@/assets/svg/progress-bar.svg'
import pwd_invisible from '@/assets/svg/pwd_invisible.svg'
import pwd_visible from '@/assets/svg/pwd_visible.svg'
import quadrantDark from '@/assets/svg/quadrant-dark.svg'
import quadrantOrigin from '@/assets/svg/quadrant-origin.svg'
import quadrant from '@/assets/svg/quadrant.svg'
import radarDark from '@/assets/svg/radar-dark.svg'
import radarOrigin from '@/assets/svg/radar-origin.svg'
import radar from '@/assets/svg/radar.svg'
import readMsg from '@/assets/svg/read-msg.svg'
import redshiftDs from '@/assets/svg/redshift-ds.svg'
import referenceField from '@/assets/svg/reference-field.svg'
import referencePlay from '@/assets/svg/reference-play.svg'
import referenceSetting from '@/assets/svg/reference-setting.svg'
import referenceTable from '@/assets/svg/reference-table.svg'
import release from '@/assets/svg/release.svg'
import report from '@/assets/svg/report.svg'
import richTextDark from '@/assets/svg/rich-text-dark.svg'
import richTextOrigin from '@/assets/svg/rich-text-origin.svg'
import richText from '@/assets/svg/rich-text.svg'
import richTextView from '@/assets/svg/richTextView.svg'
import rightJoin from '@/assets/svg/right-join.svg'
import role from '@/assets/svg/role.svg'
import round_play from '@/assets/svg/round_play.svg'
import sankeyDark from '@/assets/svg/sankey-dark.svg'
import sankeyOrigin from '@/assets/svg/sankey-origin.svg'
import sankey from '@/assets/svg/sankey.svg'
import scatterDark from '@/assets/svg/scatter-dark.svg'
import scatterOrigin from '@/assets/svg/scatter-origin.svg'
import scatter from '@/assets/svg/scatter.svg'
import scene from '@/assets/svg/scene.svg'
import scrollText from '@/assets/svg/scroll-text.svg'
import size from '@/assets/svg/size.svg'
import sortAsc from '@/assets/svg/sort-asc.svg'
import sortDesc from '@/assets/svg/sort-desc.svg'
import sqlServerDs from '@/assets/svg/sqlServer-ds.svg'
import stockLineDark from '@/assets/svg/stock-line-dark.svg'
import stockLineOrigin from '@/assets/svg/stock-line-origin.svg'
import stockLine from '@/assets/svg/stock-line.svg'
import symbolicMapDark from '@/assets/svg/symbolic-map-dark.svg'
import symbolicMapOrigin from '@/assets/svg/symbolic-map-origin.svg'
import symbolicMap from '@/assets/svg/symbolic-map.svg'
import sync from '@/assets/svg/sync.svg'
import sysParam from '@/assets/svg/sys-param.svg'
import sysParameter from '@/assets/svg/sys-parameter.svg'
import sysRelationship from '@/assets/svg/sys-relationship.svg'
import sysSetting from '@/assets/svg/sys-setting.svg'
import sysTools from '@/assets/svg/sys-tools.svg'
import system from '@/assets/svg/system.svg'
import tableInfoDark from '@/assets/svg/table-info-dark.svg'
import tableInfoOrigin from '@/assets/svg/table-info-origin.svg'
import tableInfo from '@/assets/svg/table-info.svg'
import tableNormalDark from '@/assets/svg/table-normal-dark.svg'
import tableNormalOrigin from '@/assets/svg/table-normal-origin.svg'
import tableNormal from '@/assets/svg/table-normal.svg'
import tablePivotDark from '@/assets/svg/table-pivot-dark.svg'
import tablePivotOrigin from '@/assets/svg/table-pivot-origin.svg'
import tablePivot from '@/assets/svg/table-pivot.svg'
import table from '@/assets/svg/table.svg'
import task from '@/assets/svg/task.svg'
import textDark from '@/assets/svg/text-dark.svg'
import text from '@/assets/svg/text.svg'
import titleBold from '@/assets/svg/title-bold.svg'
import titleCenter from '@/assets/svg/title-center.svg'
import titleItalic from '@/assets/svg/title-italic.svg'
import titleLeft from '@/assets/svg/title-left.svg'
import titleRight from '@/assets/svg/title-right.svg'
import titleVBottom from '@/assets/svg/title-v-bottom.svg'
import titleVCenter from '@/assets/svg/title-v-center.svg'
import titleVTop from '@/assets/svg/title-v-top.svg'
import toolboxData_fill from '@/assets/svg/toolbox-data_fill.svg'
import toolboxIcon_template from '@/assets/svg/toolbox-icon_template.svg'
import toolboxLog from '@/assets/svg/toolbox-log.svg'
import topDocDefault from '@/assets/svg/top-doc-default.svg'
import topEnterpriseTrial from '@/assets/svg/top-enterprise-trial.svg'
import topHelpDoc from '@/assets/svg/top-help-doc.svg'
import topProductBbs from '@/assets/svg/top-product-bbs.svg'
import topTechVideo from '@/assets/svg/top-tech-video.svg'
import topTechnology from '@/assets/svg/top-technology.svg'
import tree from '@/assets/svg/tree.svg'
import treemapDark from '@/assets/svg/treemap-dark.svg'
import treemapOrigin from '@/assets/svg/treemap-origin.svg'
import treemap from '@/assets/svg/treemap.svg'
import unlock from '@/assets/svg/unlock.svg'
import unpublished from '@/assets/svg/unpublished.svg'
import unreadMsg from '@/assets/svg/unread-msg.svg'
import userImg from '@/assets/svg/user-img.svg'
import userReci from '@/assets/svg/user-reci.svg'
import user from '@/assets/svg/user.svg'
import variable from '@/assets/svg/variable.svg'
import visualStar from '@/assets/svg/visual-star.svg'
import warnTree from '@/assets/svg/warn-tree.svg'
import waterfallDark from '@/assets/svg/waterfall-dark.svg'
import waterfallOrigin from '@/assets/svg/waterfall-origin.svg'
import waterfall from '@/assets/svg/waterfall.svg'
import watermark from '@/assets/svg/watermark.svg'
import webMsg from '@/assets/svg/web-msg.svg'
import wizard_enterprise from '@/assets/svg/wizard_enterprise.svg'
import wizard_help from '@/assets/svg/wizard_help.svg'
import wizard_main_bg_inner from '@/assets/svg/wizard_main_bg_inner.svg'
import wizard_quick_start from '@/assets/svg/wizard_quick_start.svg'
import wordCloudDark from '@/assets/svg/word-cloud-dark.svg'
import wordCloudOrigin from '@/assets/svg/word-cloud-origin.svg'
import wordCloud from '@/assets/svg/word-cloud.svg'
const iconMap = {
  '401': _401,
  '403': _403,
  'API-ds': APIDs,
  'Apache Hive': Apache_Hive,
  Checkbox: Checkbox,
  DM: DM,
  DataEase: DataEase,
  Elasticsearch: Elasticsearch,
  'Excel-ds': ExcelDs,
  Frame: Frame,
  KingBase: KingBase,
  Kylin: Kylin,
  Maxcompute: Maxcompute,
  PDF: PDF,
  Presto: Presto,
  'StarRocks-ds': StarRocksDs,
  'TiDB-ds': TiDBDs,
  'active-btn_copilot': activeBtn_copilot,
  add: add,
  'all-msg': allMsg,
  appearance: appearance,
  'area-dark': areaDark,
  'area-origin': areaOrigin,
  'area-stack-dark': areaStackDark,
  'area-stack-origin': areaStackOrigin,
  'area-stack': areaStack,
  area: area,
  auth: auth,
  authentication: authentication,
  'bar-dark': barDark,
  'bar-group-dark': barGroupDark,
  'bar-group-origin': barGroupOrigin,
  'bar-group-stack-dark': barGroupStackDark,
  'bar-group-stack-origin': barGroupStackOrigin,
  'bar-group-stack': barGroupStack,
  'bar-group': barGroup,
  'bar-horizontal-dark': barHorizontalDark,
  'bar-horizontal-origin': barHorizontalOrigin,
  'bar-horizontal': barHorizontal,
  'bar-origin': barOrigin,
  'bar-range-dark': barRangeDark,
  'bar-range-origin': barRangeOrigin,
  'bar-range': barRange,
  'bar-stack-dark': barStackDark,
  'bar-stack-horizontal-dark': barStackHorizontalDark,
  'bar-stack-horizontal-origin': barStackHorizontalOrigin,
  'bar-stack-horizontal': barStackHorizontal,
  'bar-stack-origin': barStackOrigin,
  'bar-stack': barStack,
  bar: bar,
  'bidirectional-bar-dark': bidirectionalBarDark,
  'bidirectional-bar-origin': bidirectionalBarOrigin,
  'bidirectional-bar': bidirectionalBar,
  board_1: board_1,
  board_2: board_2,
  board_3: board_3,
  board_4: board_4,
  board_5: board_5,
  board_6: board_6,
  board_7: board_7,
  board_8: board_8,
  board_9: board_9,
  btn_copilot: btn_copilot,
  btn_oidc: btn_oidc,
  'bubble-map-dark': bubbleMapDark,
  'bubble-map-origin': bubbleMapOrigin,
  'bubble-map': bubbleMap,
  button_right: button_right,
  cancel_release: cancel_release,
  'chart-download': chartDownload,
  'chart-mix-dark': chartMixDark,
  'chart-mix-group-dark': chartMixGroupDark,
  'chart-mix-group-origin': chartMixGroupOrigin,
  'chart-mix-group': chartMixGroup,
  'chart-mix-origin': chartMixOrigin,
  'chart-mix-stack-dark': chartMixStackDark,
  'chart-mix-stack-origin': chartMixStackOrigin,
  'chart-mix-stack': chartMixStack,
  'chart-mix': chartMix,
  'chart-table': chartTable,
  'ck-ds': ckDs,
  copilot: copilot,
  custom_sort: custom_sort,
  dark_1: dark_1,
  dashboard: dashboard,
  'data-reference': dataReference,
  database: database,
  'dataset-outline': datasetOutline,
  'dataset-task': datasetTask,
  datasource: datasource,
  'db-de': dbDe,
  'db-more-web': dbMoreWeb,
  'db2-ds': db2Ds,
  'de-api-new': deApiNew,
  'de-copy': deCopy,
  'de-db-new': deDbNew,
  'de-delete': deDelete,
  'de-ds-error': deDsError,
  'de-ds-move': deDsMove,
  'de-ds-rename': deDsRename,
  'de-ds-trash': deDsTrash,
  'de-ds-warning': deDsWarning,
  'de-excel-new': deExcelNew,
  'de-json': deJson,
  'de-move': deMove,
  'de-search': deSearch,
  'de-sql-new': deSqlNew,
  'de-union-new': deUnionNew,
  de_pwd_invisible: de_pwd_invisible,
  de_pwd_visible: de_pwd_visible,
  default_avatar: default_avatar,
  delete: _delete,
  'display-setting': displaySetting,
  doc: doc,
  docs: docs,
  'doris-ds': dorisDs,
  drag: drag,
  'driver-de': driverDe,
  'ds-api': dsApi,
  'ds-custom': dsCustom,
  'ds-db': dsDb,
  'ds-excel': dsExcel,
  'ds-sql': dsSql,
  'ds-union': dsUnion,
  'dv-ai-window-max': dvAiWindowMax,
  'dv-ai-window-min': dvAiWindowMin,
  'dv-ai': dvAi,
  'dv-bar-enlarge': dvBarEnlarge,
  'dv-bar-unLinkage': dvBarUnLinkage,
  'dv-batch': dvBatch,
  'dv-copy-dark': dvCopyDark,
  'dv-copy': dvCopy,
  'dv-dashboard-spine-mobile': dvDashboardSpineMobile,
  'dv-dashboard-spine': dvDashboardSpine,
  'dv-dashboard': dvDashboard,
  'dv-delete': dvDelete,
  'dv-details': dvDetails,
  'dv-drag-tips': dvDragTips,
  'dv-edit': dvEdit,
  'dv-empty': dvEmpty,
  'dv-expand-down': dvExpandDown,
  'dv-expand-right': dvExpandRight,
  'dv-eye-close': dvEyeClose,
  'dv-filter-show': dvFilterShow,
  'dv-filter': dvFilter,
  'dv-folder': dvFolder,
  'dv-head-more': dvHeadMore,
  'dv-info': dvInfo,
  'dv-link-target': dvLinkTarget,
  'dv-lock': dvLock,
  'dv-material': dvMaterial,
  'dv-max': dvMax,
  'dv-media': dvMedia,
  'dv-min': dvMin,
  'dv-more-com': dvMoreCom,
  'dv-more-time-clock': dvMoreTimeClock,
  'dv-more': dvMore,
  'dv-move': dvMove,
  'dv-new-folder': dvNewFolder,
  'dv-new': dvNew,
  'dv-no-img': dvNoImg,
  'dv-nothing': dvNothing,
  'dv-params': dvParams,
  'dv-picture-real': dvPictureReal,
  'dv-picture-show': dvPictureShow,
  'dv-picture': dvPicture,
  'dv-preview-download': dvPreviewDownload,
  'dv-preview-inner': dvPreviewInner,
  'dv-preview-outer': dvPreviewOuter,
  'dv-preview': dvPreview,
  'dv-rename': dvRename,
  'dv-reposition': dvReposition,
  'dv-richText': dvRichText,
  'dv-ruler': dvRuler,
  'dv-screen-new': dvScreenNew,
  'dv-screen-spine': dvScreenSpine,
  'dv-scroll-text': dvScrollText,
  'dv-share': dvShare,
  'dv-show': dvShow,
  'dv-sort-asc': dvSortAsc,
  'dv-sort-desc': dvSortDesc,
  'dv-style-activeFont': dvStyleActiveFont,
  'dv-style-activeFontSize': dvStyleActiveFontSize,
  'dv-style-backgroundColor': dvStyleBackgroundColor,
  'dv-style-borderColor': dvStyleBorderColor,
  'dv-style-borderRadius': dvStyleBorderRadius,
  'dv-style-borderSize': dvStyleBorderSize,
  'dv-style-borderStyle': dvStyleBorderStyle,
  'dv-style-color': dvStyleColor,
  'dv-style-fontFamily': dvStyleFontFamily,
  'dv-style-fontSize': dvStyleFontSize,
  'dv-style-fontWeight': dvStyleFontWeight,
  'dv-style-headBorderActiveColor': dvStyleHeadBorderActiveColor,
  'dv-style-headBorderColor': dvStyleHeadBorderColor,
  'dv-style-headFontActiveColor': dvStyleHeadFontActiveColor,
  'dv-style-headFontColor': dvStyleHeadFontColor,
  'dv-style-headHorizontalPosition': dvStyleHeadHorizontalPosition,
  'dv-style-letterSpacing': dvStyleLetterSpacing,
  'dv-style-lineHeight': dvStyleLineHeight,
  'dv-style-opacity': dvStyleOpacity,
  'dv-style-scroll-speed': dvStyleScrollSpeed,
  'dv-style-textAlign': dvStyleTextAlign,
  'dv-tab-show': dvTabShow,
  'dv-tab': dvTab,
  'dv-text': dvText,
  'dv-unlock': dvUnlock,
  'dv-up-arrow': dvUpArrow,
  'dv-use-template': dvUseTemplate,
  'dv-video': dvVideo,
  'dv-view': dvView,
  dv_mobile_layout: dv_mobile_layout,
  'edit-done': editDone,
  'edit-in': editIn,
  edit: edit,
  'email-task': emailTask,
  embedded: embedded,
  example: example,
  exclamationmark: exclamationmark,
  exclamationmark2: exclamationmark2,
  'exit-fullscreen': exitFullscreen,
  'eye-open': eyeOpen,
  eye: eye,
  field_location: field_location,
  field_text: field_text,
  field_time: field_time,
  field_value: field_value,
  'filter-center': filterCenter,
  'filter-h-center': filterHCenter,
  'filter-h-left': filterHLeft,
  'filter-h-right': filterHRight,
  'filter-top': filterTop,
  filter: filter,
  'flow-map-dark': flowMapDark,
  'flow-map-origin': flowMapOrigin,
  'flow-map': flowMap,
  folder: folder,
  form: form,
  fullscreen: fullscreen,
  'funnel-dark': funnelDark,
  'funnel-origin': funnelOrigin,
  funnel: funnel,
  'gauge-dark': gaugeDark,
  'gauge-origin': gaugeOrigin,
  gauge: gauge,
  'graphical-circular': graphicalCircular,
  'graphical-rect': graphicalRect,
  'graphical-triangle': graphicalTriangle,
  'group-3400': group3400,
  group: group,
  'heat-map-dark': heatMapDark,
  'heat-map-origin': heatMapOrigin,
  'heat-map': heatMap,
  'icon-ctrl-close': iconCtrlClose,
  'icon-outline-notification': iconOutlineNotification,
  'icon-alarmclock': iconAlarmclock,
  'icon-contacts': iconContacts,
  'icon-draft': iconDraft,
  'icon-filter': iconFilter,
  'icon-group': iconGroup,
  'icon-image-upload': iconImageUpload,
  'icon-image': iconImage,
  'icon-laser': iconLaser,
  'icon-lock': iconLock,
  'icon-maybe': iconMaybe,
  'icon-maybe_outlined': iconMaybe_outlined,
  'icon-more': iconMore,
  'icon-quicksetting': iconQuicksetting,
  'icon-setting': iconSetting,
  'icon-stream': iconStream,
  'icon-video': iconVideo,
  icon_Batch_outlined: icon_Batch_outlined,
  icon_Invalid_colorful: icon_Invalid_colorful,
  'icon_add-dictionary_outlined': icon_addDictionary_outlined,
  'icon_add-entry_outlined': icon_addEntry_outlined,
  'icon_add-folder_outlined': icon_addFolder_outlined,
  'icon_add_outlined-1': icon_add_outlined1,
  icon_add_outlined: icon_add_outlined,
  icon_admin_outlined: icon_admin_outlined,
  'icon_api-outlined': icon_apiOutlined,
  icon_api: icon_api,
  icon_app_outlined: icon_app_outlined,
  'icon_arrow-right_outlined': icon_arrowRight_outlined,
  icon_assigned_outlined: icon_assigned_outlined,
  icon_attachment_outlined: icon_attachment_outlined,
  icon_bold_outlined: icon_bold_outlined,
  'icon_bottom-align_outlined': icon_bottomAlign_outlined,
  icon_calendar_outlined: icon_calendar_outlined,
  icon_cancel_store: icon_cancel_store,
  icon_card_outlined: icon_card_outlined,
  'icon_center-alignment_outlined': icon_centerAlignment_outlined,
  'icon_chart-line-c': icon_chartLineC,
  'icon_chart-line': icon_chartLine,
  icon_clear_outlined: icon_clear_outlined,
  icon_close_filled: icon_close_filled,
  icon_close_outlined: icon_close_outlined,
  icon_collect_filled: icon_collect_filled,
  icon_collection_outlined: icon_collection_outlined,
  icon_copy_filled: icon_copy_filled,
  icon_copy_outlined: icon_copy_outlined,
  icon_customize: icon_customize,
  icon_dashboard: icon_dashboard,
  'icon_dashboard_outlined-c': icon_dashboard_outlinedC,
  icon_dashboard_outlined: icon_dashboard_outlined,
  'icon_database-alert_filled': icon_databaseAlert_filled,
  'icon_database-fail_filled': icon_databaseFail_filled,
  icon_database: icon_database,
  icon_database_outlined: icon_database_outlined,
  icon_dataset: icon_dataset,
  icon_dataset_outlined: icon_dataset_outlined,
  icon_db_filled: icon_db_filled,
  'icon_delete-trash_outlined': icon_deleteTrash_outlined,
  icon_describe_outlined: icon_describe_outlined,
  icon_dialpad_outlined: icon_dialpad_outlined,
  'icon_disorde-list_outlined': icon_disordeList_outlined,
  icon_divider_outlined: icon_divider_outlined,
  'icon_doc-replace_outlined': icon_docReplace_outlined,
  icon_done_outlined: icon_done_outlined,
  'icon_down-right_outlined': icon_downRight_outlined,
  'icon_down_outlined-1': icon_down_outlined1,
  icon_down_outlined: icon_down_outlined,
  icon_download_outlined: icon_download_outlined,
  icon_drag_outlined: icon_drag_outlined,
  icon_drilling_outlined: icon_drilling_outlined,
  icon_drive_filled: icon_drive_filled,
  icon_edit_outlined: icon_edit_outlined,
  icon_effects_outlined: icon_effects_outlined,
  icon_excel: icon_excel,
  icon_excel_outlined: icon_excel_outlined,
  'icon_expand-down_filled': icon_expandDown_filled,
  'icon_expand-left_filled': icon_expandLeft_filled,
  'icon_expand-right_filled': icon_expandRight_filled,
  'icon_file-add_outlined': icon_fileAdd_outlined,
  'icon_file-excel_colorful': icon_fileExcel_colorful,
  icon_folder_filled: icon_folder_filled,
  'icon_font-color_outlined': icon_fontColor_outlined,
  icon_form_outlined: icon_form_outlined,
  'icon_full-association': icon_fullAssociation,
  icon_functions_outlined: icon_functions_outlined,
  icon_graphical: icon_graphical,
  icon_gridlines_outlined: icon_gridlines_outlined,
  icon_h1_outlined: icon_h1_outlined,
  icon_h2_outlined: icon_h2_outlined,
  icon_h3_outlined: icon_h3_outlined,
  icon_hn_outlined: icon_hn_outlined,
  'icon_horizontal-align_outlined': icon_horizontalAlign_outlined,
  icon_info_colorful: icon_info_colorful,
  icon_info_filled: icon_info_filled,
  icon_info_outlined: icon_info_outlined,
  icon_intersect: icon_intersect,
  'icon_into-item_outlined': icon_intoItem_outlined,
  icon_invisible_outlined: icon_invisible_outlined,
  icon_italic_outlined: icon_italic_outlined,
  'icon_left-align_outlined': icon_leftAlign_outlined,
  'icon_left-alignment_outlined': icon_leftAlignment_outlined,
  'icon_left-association': icon_leftAssociation,
  icon_left_outlined: icon_left_outlined,
  'icon_letter-spacing_outlined': icon_letterSpacing_outlined,
  'icon_link-record': icon_linkRecord,
  'icon_link-record_outlined-1': icon_linkRecord_outlined1,
  'icon_link-record_outlined': icon_linkRecord_outlined,
  icon_loading_outlined: icon_loading_outlined,
  icon_local: icon_local,
  icon_local_outlined: icon_local_outlined,
  'icon_logs_outlined-1': icon_logs_outlined1,
  icon_logs_outlined: icon_logs_outlined,
  icon_magnify_outlined: icon_magnify_outlined,
  'icon_member-add_outlined': icon_memberAdd_outlined,
  icon_member_filled: icon_member_filled,
  icon_minify_outlined: icon_minify_outlined,
  'icon_moments-categories_outlined': icon_momentsCategories_outlined,
  'icon_more-vertical_outlined': icon_moreVertical_outlined,
  icon_more_outlined: icon_more_outlined,
  'icon_new-item_outlined': icon_newItem_outlined,
  icon_number_outlined: icon_number_outlined,
  'icon_operation-analysis_outlined': icon_operationAnalysis_outlined,
  'icon_orde-list_outlined': icon_ordeList_outlined,
  icon_organization_outlined: icon_organization_outlined,
  icon_params_setting: icon_params_setting,
  icon_pc_fullscreen: icon_pc_fullscreen,
  icon_pc_outlined: icon_pc_outlined,
  icon_pc_outlined_copy: icon_pc_outlined_copy,
  icon_phone_outlined: icon_phone_outlined,
  'icon_pie_outlined-c': icon_pie_outlinedC,
  'icon_play-round_filled': icon_playRound_filled,
  'icon_play-round_outlined': icon_playRound_outlined,
  icon_plugin_outlined: icon_plugin_outlined,
  'icon_pull-left_outlined': icon_pullLeft_outlined,
  'icon_pull-right_outlined': icon_pullRight_outlined,
  icon_qr_outlined: icon_qr_outlined,
  icon_redo_outlined: icon_redo_outlined,
  icon_refresh_outlined: icon_refresh_outlined,
  icon_rename_outlined: icon_rename_outlined,
  icon_replace_outlined: icon_replace_outlined,
  icon_reset_outlined: icon_reset_outlined,
  icon_resetpassword: icon_resetpassword,
  'icon_right-align_outlined': icon_rightAlign_outlined,
  'icon_right-alignment_outlined': icon_rightAlignment_outlined,
  'icon_right-association': icon_rightAssociation,
  icon_right_outlined: icon_right_outlined,
  icon_scroll_filled: icon_scroll_filled,
  'icon_search-outline_outlined': icon_searchOutline_outlined,
  icon_search: icon_search,
  'icon_share-label_filled': icon_shareLabel_filled,
  'icon_share-label_outlined': icon_shareLabel_outlined,
  'icon_side-expand_outlined': icon_sideExpand_outlined,
  'icon_side-fold_outlined': icon_sideFold_outlined,
  'icon_sort-a-to-z_outlined': icon_sortAToZ_outlined,
  'icon_sort-z-to-a_outlined': icon_sortZToA_outlined,
  icon_sort_outlined: icon_sort_outlined,
  icon_sql: icon_sql,
  icon_sql_outlined: icon_sql_outlined,
  icon_sql_outlined_1: icon_sql_outlined_1,
  icon_stretch_outlined: icon_stretch_outlined,
  'icon_style-set_outlined': icon_styleSet_outlined,
  icon_succeed_colorful: icon_succeed_colorful,
  icon_succeed_filled: icon_succeed_filled,
  icon_switch_outlined: icon_switch_outlined,
  'icon_sync-play-round_filled': icon_syncPlayRound_filled,
  'icon_sync-play-round_outlined': icon_syncPlayRound_outlined,
  icon_sync_close_log_details: icon_sync_close_log_details,
  icon_sync_datasource: icon_sync_datasource,
  icon_sync_log_number: icon_sync_log_number,
  icon_sync_logs_outlined: icon_sync_logs_outlined,
  icon_sync_progress: icon_sync_progress,
  icon_sync_target_to_datasource: icon_sync_target_to_datasource,
  icon_sync_task_number: icon_sync_task_number,
  'icon_take-action_outlined': icon_takeAction_outlined,
  'icon_team-add_outlined': icon_teamAdd_outlined,
  icon_template: icon_template,
  icon_template_colorful: icon_template_colorful,
  icon_template_outlined: icon_template_outlined,
  'icon_text-box_outlined': icon_textBox_outlined,
  icon_text_outlined: icon_text_outlined,
  icon_time_outlined: icon_time_outlined,
  'icon_title-left-align_outlined': icon_titleLeftAlign_outlined,
  'icon_title-top-align_outlined': icon_titleTopAlign_outlined,
  'icon_top-align_outlined': icon_topAlign_outlined,
  icon_undo_outlined: icon_undo_outlined,
  'icon_up-left_outlined': icon_upLeft_outlined,
  icon_upload_outlined: icon_upload_outlined,
  'icon_vertical-align_outlined': icon_verticalAlign_outlined,
  'icon_view-list_outlined': icon_viewList_outlined,
  icon_viewinchat_outlined: icon_viewinchat_outlined,
  icon_visible_outlined: icon_visible_outlined,
  icon_warning_colorful: icon_warning_colorful,
  icon_warning_filled: icon_warning_filled,
  icon_yes_outlined: icon_yes_outlined,
  iconfont: iconfont,
  'impala-ds': impalaDs,
  'indicator-dark': indicatorDark,
  'indicator-origin': indicatorOrigin,
  indicator: indicator,
  'inner-join': innerJoin,
  international: international,
  'join-join': joinJoin,
  label: label,
  language: language,
  'left-join': leftJoin,
  'line-dark': lineDark,
  'line-origin': lineOrigin,
  line: line,
  'link-back': linkBack,
  'link-down': linkDown,
  link: link,
  'liquid-dark': liquidDark,
  'liquid-origin': liquidOrigin,
  liquid: liquid,
  lock_closed: lock_closed,
  lock_closed_switch: lock_closed_switch,
  lock_open: lock_open,
  lock_open_back: lock_open_back,
  lock_other_open: lock_other_open,
  log: log,
  logo: logo,
  logo_cas: logo_cas,
  logo_ldap: logo_ldap,
  logo_dingtalk: logo_dingtalk,
  logo_lark: logo_lark,
  logo_oauth: logo_oauth,
  'logo_wechat-work': logo_wechatWork,
  'map-dark': mapDark,
  'map-origin': mapOrigin,
  map: map,
  map_mini: map_mini,
  map_old: map_old,
  'mariadb-ds': mariadbDs,
  'market-expand': marketExpand,
  'market-retract': marketRetract,
  menu: menu,
  'mobile-icon_dashboard_filled': mobileIcon_dashboard_filled,
  'mobile-icon_dashboard_outlined': mobileIcon_dashboard_outlined,
  'mobile-icon_home_filled': mobileIcon_home_filled,
  'mobile-icon_home_outlined': mobileIcon_home_outlined,
  'mobile-icon_member_filled': mobileIcon_member_filled,
  'mobile-icon_member_outlined': mobileIcon_member_outlined,
  'mobile-checkbox': mobileCheckbox,
  'mongo-ds': mongoDs,
  more_v: more_v,
  move: move,
  'msg-setting': msgSetting,
  'mysql-ds': mysqlDs,
  'mysql-frame': mysqlFrame,
  nested: nested,
  'no-join': noJoin,
  no_result: no_result,
  'non-existent': nonExistent,
  none_select: none_select,
  notification: notification,
  'oracle-ds': oracleDs,
  org: org,
  other_material: other_material,
  other_material_board: other_material_board,
  other_material_icon: other_material_icon,
  other_media: other_media,
  other_setting: other_setting,
  other_text: other_text,
  'page-line': pageLine,
  'panel-mobile-publish': panelMobilePublish,
  'panel-mobile-unpublished-back': panelMobileUnpublishedBack,
  'panel-mobile-unpublished': panelMobileUnpublished,
  'panel-publish': panelPublish,
  'panel-unpublished-back': panelUnpublishedBack,
  'panel-unpublished': panelUnpublished,
  panel: panel,
  people: people,
  peoples: peoples,
  'percentage-bar-stack-dark': percentageBarStackDark,
  'percentage-bar-stack-horizontal-dark': percentageBarStackHorizontalDark,
  'percentage-bar-stack-horizontal-origin': percentageBarStackHorizontalOrigin,
  'percentage-bar-stack-horizontal': percentageBarStackHorizontal,
  'percentage-bar-stack-origin': percentageBarStackOrigin,
  'percentage-bar-stack': percentageBarStack,
  'pg-ds': pgDs,
  'pie-dark': pieDark,
  'pie-donut-dark': pieDonutDark,
  'pie-donut-origin': pieDonutOrigin,
  'pie-donut-rose-dark': pieDonutRoseDark,
  'pie-donut-rose-origin': pieDonutRoseOrigin,
  'pie-donut-rose': pieDonutRose,
  'pie-donut': pieDonut,
  'pie-origin': pieOrigin,
  'pie-rose-dark': pieRoseDark,
  'pie-rose-origin': pieRoseOrigin,
  'pie-rose': pieRose,
  pie: pie,
  platform: platform,
  'plugin-3dmap': plugin3dmap,
  'plugin-3dpie': plugin3dpie,
  'plugin-dameng': pluginDameng,
  'plugin-default': pluginDefault,
  plugin: plugin,
  'plugins-new': pluginsNew,
  plugins: plugins,
  'progress-bar-dark': progressBarDark,
  'progress-bar-origin': progressBarOrigin,
  'progress-bar': progressBar,
  pwd_invisible: pwd_invisible,
  pwd_visible: pwd_visible,
  'quadrant-dark': quadrantDark,
  'quadrant-origin': quadrantOrigin,
  quadrant: quadrant,
  'radar-dark': radarDark,
  'radar-origin': radarOrigin,
  radar: radar,
  'read-msg': readMsg,
  'redshift-ds': redshiftDs,
  'reference-field': referenceField,
  'reference-play': referencePlay,
  'reference-setting': referenceSetting,
  'reference-table': referenceTable,
  release: release,
  report: report,
  'rich-text-dark': richTextDark,
  'rich-text-origin': richTextOrigin,
  'rich-text': richText,
  richTextView: richTextView,
  'right-join': rightJoin,
  role: role,
  round_play: round_play,
  'sankey-dark': sankeyDark,
  'sankey-origin': sankeyOrigin,
  sankey: sankey,
  'scatter-dark': scatterDark,
  'scatter-origin': scatterOrigin,
  scatter: scatter,
  scene: scene,
  'scroll-text': scrollText,
  size: size,
  'sort-asc': sortAsc,
  'sort-desc': sortDesc,
  'sqlServer-ds': sqlServerDs,
  'stock-line-dark': stockLineDark,
  'stock-line-origin': stockLineOrigin,
  'stock-line': stockLine,
  'symbolic-map-dark': symbolicMapDark,
  'symbolic-map-origin': symbolicMapOrigin,
  'symbolic-map': symbolicMap,
  sync: sync,
  'sys-param': sysParam,
  'sys-parameter': sysParameter,
  'sys-relationship': sysRelationship,
  'sys-setting': sysSetting,
  'sys-tools': sysTools,
  system: system,
  'table-info-dark': tableInfoDark,
  'table-info-origin': tableInfoOrigin,
  'table-info': tableInfo,
  'table-normal-dark': tableNormalDark,
  'table-normal-origin': tableNormalOrigin,
  'table-normal': tableNormal,
  'table-pivot-dark': tablePivotDark,
  'table-pivot-origin': tablePivotOrigin,
  'table-pivot': tablePivot,
  table: table,
  task: task,
  'text-dark': textDark,
  text: text,
  'title-bold': titleBold,
  'title-center': titleCenter,
  'title-italic': titleItalic,
  'title-left': titleLeft,
  'title-right': titleRight,
  'title-v-bottom': titleVBottom,
  'title-v-center': titleVCenter,
  'title-v-top': titleVTop,
  'toolbox-data_fill': toolboxData_fill,
  'toolbox-icon_template': toolboxIcon_template,
  'toolbox-log': toolboxLog,
  'top-doc-default': topDocDefault,
  'top-enterprise-trial': topEnterpriseTrial,
  'top-help-doc': topHelpDoc,
  'top-product-bbs': topProductBbs,
  'top-tech-video': topTechVideo,
  'top-technology': topTechnology,
  tree: tree,
  'treemap-dark': treemapDark,
  'treemap-origin': treemapOrigin,
  treemap: treemap,
  unlock: unlock,
  unpublished: unpublished,
  'unread-msg': unreadMsg,
  'user-img': userImg,
  'user-reci': userReci,
  user: user,
  variable: variable,
  'visual-star': visualStar,
  'warn-tree': warnTree,
  'waterfall-dark': waterfallDark,
  'waterfall-origin': waterfallOrigin,
  waterfall: waterfall,
  watermark: watermark,
  'web-msg': webMsg,
  wizard_enterprise: wizard_enterprise,
  wizard_help: wizard_help,
  wizard_main_bg_inner: wizard_main_bg_inner,
  wizard_quick_start: wizard_quick_start,
  'word-cloud-dark': wordCloudDark,
  'word-cloud-origin': wordCloudOrigin,
  'word-cloud': wordCloud
}

const props = defineProps({
  prefix: propTypes.string.def('icon'),
  name: propTypes.string,
  className: propTypes.string,
  staticContent: propTypes.string
})
const svgClass = computed(() => {
  if (props.className) {
    return `svg-icon ${props.className}`
  }
  return 'svg-icon'
})
</script>

<template>
  <div
    class="svg-container"
    v-if="staticContent"
    v-html="staticContent"
    :class="svgClass"
    aria-hidden="true"
  ></div>
  <component v-else :class="svgClass" :is="iconMap[name]"></component>
</template>
<style lang="less" scope>
.svg-icon {
  overflow: hidden;
  vertical-align: -0.1em; /* 因icon大小被设置为和字体大小一致，而span等标签的下边缘会和字体的基线对齐，故需设置一个往下的偏移比例，来纠正视觉上的未对齐效果 */
  fill: currentcolor; /* 定义元素的颜色，currentColor是一个变量，这个变量的值就表示当前元素的color值，如果当前元素未设置color值，则从父元素继承 */
}
.svg-container {
  width: 100%;
  height: 100%;
  svg {
    overflow: hidden;
    vertical-align: -0.1em;
    fill: currentcolor;
    width: 100%;
    height: 100%;
  }
}
</style>
