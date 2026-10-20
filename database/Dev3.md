# Necessary Extensions and Terminal Controls for starting the SQL file

# Extensions:
- SQLTools | v0.28.6(By: Matheus Teixeira)
- SQLTools MySQL/MariaDB/TiDB | v0.6.7 (By: Matheus Teixeira) 

# Current Database User and Password
- Username: "Dev3" , Password: "Dev3Password"

# Terminal commands for a new Codespace

- cd /workspaces/AdaptiveReadingGame
- sudo apt update
- sudo apt install -y mysql-server
- sudo service mysql start
- sleep 10
- export DB_PASS='Dev3Password'
- sudo mysql -e "CREATE DATABASE IF NOT EXISTS adaptive_reading;"
- sudo mysql -e "CREATE USER IF NOT EXISTS 'Dev3'@'localhost' IDENTIFIED BY '$DB_PASS';"
- sudo mysql -e "GRANT ALL PRIVILEGES ON adaptive_reading.* TO 'Dev3'@'localhost'; FLUSH PRIVILEGES;"
- bash database/Setup.sh
- mvn -B -DskipTests compile
- mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt

# Terminal commands for a reopening the same codespace

- cd /workspaces/AdaptiveReadingGame
( Opens the correct directory to start the MySQL)
- sudo service mysql start
( Start the MySQL )
- sleep 5
( Wait)
- sudo service mysql status
( check the mysql status if it is working)
- export DB_PASS='Dev3Password'
( Export the password )

# Terminal commands if theres an error on starting, Fix the socket permision
If theres an error "ERROR 2002 (HY000)" do these:
- sudo mkdir -p /var/run/mysqld
- sudo chown mysql:mysql /var/run/mysqld
- sudo chmod 755 /var/run/mysqld
- sudo service mysql restart
- sleep 10
- sudo mysqladmin ping

# Test The login (not neccessary)
- export DB_USER='Dev3'
- export DB_PASS='Dev3Password'
- MYSQL_PWD="$DB_PASS" mysql -u "$DB_USER" -e "SELECT 1;"
( If the output is "1" then its good )

# Check if the files is in place
- ls database database/migrations database/seed
- ls src/main/java/com/adaptivereadinggame/repository/jdbc

# Test The file with java
- java -cp "target/classes:$(cat /tmp/cp.txt)" com.adaptivereadinggame.repository.jdbc.DbFullCheck
( Checks the database )
- java -cp "target/classes:$(cat /tmp/cp.txt)" com.adaptivereadinggame.repository.jdbc.StoryPlayTest
( This will run the code )

# SQL Tools Connection
Enter these informations when creating connection
- Connection name: adaptive_reading
- Server: localhost
- Port: 3306
- Database: adaptive_reading
- Username: "Dev3"
At the top box, it will ask for password. Enter the password that was set ("Dev3Password") then press enter. (The box may not show what your typing to hide the password)