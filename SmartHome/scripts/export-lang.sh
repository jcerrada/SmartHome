cd ../httpdocs
if [ ! -d 'sites/all/translations' ]; then
	mkdir sites/all/translations/
fi
rm -rf sites/all/translations/*
echo 'Exportando traducciones'
drush language-export es sites/all/translations/interface.po
drush language-export es sites/all/translations/rules.po --group=rules
drush language-export es sites/all/translations/contact.po --group=contact
drush language-export es sites/all/translations/fields.po --group=field
drush language-export es sites/all/translations/nodetypes.po --group=node
drush language-export es sites/all/translations/metatag.po --group=metatag
drush language-export es sites/all/translations/webformloc.po --group=webform
drush language-export es sites/all/translations/menu.po --group=menu
drush language-export es sites/all/translations/taxonomy.po --group=taxonomy
drush language-export es sites/all/translations/blocks.po --group=blocks
echo 'Exportación finalizada'