cd ..
git pull origin master
cd httpdocs
drush make --no-core ../config/project.make .
drush en -y features
drush features-revert-all
drush updb
drush cc all
