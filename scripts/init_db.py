import mysql.connector

# 读取 SQL 文件
with open('src/main/resources/schema.sql', 'r', encoding='utf-8') as f:
    sql_content = f.read()

# 连接 MySQL
connection = mysql.connector.connect(
    host='152.136.123.201',
    port=3306,
    user='root',
    password='mysql_Xdwzfi',
    charset='utf8mb4',
    connection_timeout=30,
    use_pure=True,
    ssl_disabled=True
)

try:
    cursor = connection.cursor()
    
    # 创建数据库
    cursor.execute("CREATE DATABASE IF NOT EXISTS `indie_station` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    print("✅ 数据库创建成功")
    
    cursor.execute("USE `indie_station`")
    
    # 按分号分割，逐条执行
    statements = []
    current = []
    for line in sql_content.split('\n'):
        stripped = line.strip()
        if stripped.startswith('--') or stripped.startswith('/*') or not stripped:
            continue
        if 'CREATE DATABASE' in line or 'USE ' in line:
            continue
        
        current.append(line)
        if stripped.endswith(';'):
            sql = '\n'.join(current).strip()
            if sql and sql != ';':
                statements.append(sql)
            current = []
    
    success_count = 0
    error_count = 0
    for sql in statements:
        try:
            cursor.execute(sql)
            success_count += 1
        except Exception as e:
            if 'already exists' in str(e) or 'Duplicate' in str(e):
                print(f"⚠️ 已存在，跳过: {str(e)[:80]}")
            else:
                print(f"❌ 执行失败: {str(e)[:100]}")
                print(f"   SQL: {sql[:100]}...")
                error_count += 1
    
    connection.commit()
    print(f"\n✅ 执行完成! 成功: {success_count}, 错误: {error_count}")
    
    # 验证
    cursor.execute("SHOW TABLES")
    tables = cursor.fetchall()
    print(f"\n📊 数据库中的表 ({len(tables)}个):")
    for t in tables:
        print(f"  - {t[0]}")
    
    cursor.execute("SELECT id, username, nickname, role, status FROM t_admin")
    admins = cursor.fetchall()
    print(f"\n👤 管理员账号 ({len(admins)}个):")
    for a in admins:
        print(f"  - {a[1]} ({a[2]}) - 角色: {a[3]}, 状态: {a[4]}")
    
    cursor.execute("SELECT COUNT(*) FROM t_menu")
    print(f"\n📋 菜单数据: {cursor.fetchone()[0]} 条")
    
    cursor.execute("SELECT COUNT(*) FROM t_site_config")
    print(f"⚙️ 系统配置: {cursor.fetchone()[0]} 项")

finally:
    connection.close()
    print("\n🔒 数据库连接已关闭")
