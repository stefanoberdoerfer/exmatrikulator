Installing the scripts:

All scripts in this directory have to be copied to the root directory of your
glassfish server installation in order to work. For example if you have
installed your glassfish server to /opt/, the root directory should be
/opt/glassfish4/.


Running the Scripts:

All scripts have to be run from the root directory of your glassfish server
installation to function correctly. The .bat scripts are meant for windows
machines, while the .sh files are meant for *nix machines. You need to supply
the install scripts with a new password for the database connection they are
going to create. The password is supplied through the first command line
argument. In Order to restore an older state from a backup you need to run the
restore script, which takes the full name of the backup you created prior
e.g. ScheduledBackup_2016-03-02_14-00-00 as it's first command line argument.
Please note that running the restore script will shut down your glassfish server
and your derby server for a short while. If you should change anything regarding
the structure of the directories or the name of the database, these changes will
have to be also made to the scripts.
