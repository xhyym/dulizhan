import mysql.connector
import bcrypt

conn = mysql.connector.connect(host='152.136.123.201', port=3306, user='root', password='mysql_Xdwzfi', database='indie_station', ssl_disabled=True)
cursor = conn.cursor()

# 生成正确的 BCrypt hash (admin123)
password = 'admin123'
hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
# Python bcrypt 用 $2b$，Java 用 $2a$，需要转换
java_hash = hashed.replace('$2b$', '$2a$')

print(f"Generated hash: {java_hash}")

cursor.execute('UPDATE t_admin SET password = %s', (java_hash,))
conn.commit()
print(f'Updated {cursor.rowcount} admin records')

# 验证
cursor.execute('SELECT username, nickname, password FROM t_admin')
for row in cursor.fetchall():
    print(f'{row[0]} ({row[1]}): {row[1][:40]}...')

conn.close()
