UPDATE `my_plugin`
SET `version` = '1.18.3'
where `plugin_id` > 0
  and `version` = '1.18.2';
UPDATE `panel_pdf_template` SET `name` = 'I18N_PANEL_PDF_TEMPLATE_WITH_PARAMS',`template_content` = '<div style=\"margin-top: 5px\">\n    <div contenteditable=\"true\"> \n		$t(''panel.panel_name'')：$panelName$ </br>\n		$t(''panel.export_time'')：$yyyy-MM-dd hh:mm:ss$ </br>\n		$t(''panel.export_user'')：$nickName$ </br>\n		$t(''panel.you_can_type_here'')\n		</div>\n    <div>\n      <img width=\"100%\" src=\"$snapshot$\">\n    </div>\n  </div>' WHERE id='1';
UPDATE `panel_pdf_template` SET `name` = 'I18N_PANEL_PDF_TEMPLATE_ONLY_PIC' WHERE id='2';
