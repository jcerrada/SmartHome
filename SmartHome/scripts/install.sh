mkdir ../httpdocs/sites/default
cp ../config/settings.php ../httpdocs/sites/default/settings.php
cd ../httpdocs
drush make profiles/smarthome/base.make .
drush make --no-core ../config/project.make .
drush si smarthome --account-name=admin --account-pass=admin
mkdir sites/default/files
chmod 775 -R sites/default/files
drush en -y feature_sitewide
drush features-revert-all
drush cc all