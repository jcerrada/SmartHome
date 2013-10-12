cd ../httpdocs
echo 'Starting importation'
drush language-import es sites/all/translations/interface.po --replace
drush language-import es sites/all/translations/rules.po --replace --group=rules
drush language-import es sites/all/translations/contact.po --replace --group=contact
drush language-import es sites/all/translations/fields.po --replace --group=field
drush language-import es sites/all/translations/nodetypes.po --replace --group=node
drush language-import es sites/all/translations/metatag.po --replace --group=metatag
drush language-import es sites/all/translations/webformloc.po --replace --group=webform
drush language-import es sites/all/translations/menu.po --replace --group=menu
drush language-import es sites/all/translations/taxonomy.po --replace --group=taxonomy
drush language-import es sites/all/translations/blocks.po --replace --group=blocks
echo 'Importation finished'