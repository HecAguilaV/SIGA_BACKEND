#!/usr/bin/env python3
"""
Script para ejecutar la migración de permisos en la base de datos.
Ejecutar: python scripts/ejecutar_migracion_permisos.py
"""

import psycopg2
import os
from pathlib import Path

# Configuración de la base de datos (AlwaysData)
DB_CONFIG = {
    'host': 'postgresql-hector.alwaysdata.net',
    'port': 5432,
    'dbname': 'hector_siga_db',
    'user': 'hector',
    'password': 'kike4466'  # ⚠️ Cambiar si es necesario
}

def ejecutar_migracion():
    """Ejecuta la migración 008_create_sistema_permisos.sql"""
    
    # Obtener la ruta del script SQL
    script_dir = Path(__file__).parent.parent
    sql_file = script_dir / 'src' / 'main' / 'resources' / 'db' / 'migrations' / '008_create_sistema_permisos.sql'
    
    if not sql_file.exists():
        print(f"❌ Error: No se encontró el archivo {sql_file}")
        return False
    
    print(f"📄 Leyendo archivo: {sql_file}")
    
    try:
        # Leer el contenido del SQL
        with open(sql_file, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        
        print("🔌 Conectando a la base de datos...")
        
        # Conectar a la base de datos
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = True
        cur = conn.cursor()
        
        print("⚙️  Ejecutando migración...")
        
        # Ejecutar el SQL
        cur.execute(sql_content)
        
        print("✅ Migración ejecutada exitosamente")
        
        # Verificar que las tablas se crearon
        print("\n🔍 Verificando tablas creadas...")
        
        cur.execute("SELECT COUNT(*) FROM siga_saas.PERMISOS")
        count_permisos = cur.fetchone()[0]
        print(f"   ✓ PERMISOS: {count_permisos} permisos insertados")
        
        cur.execute("SELECT COUNT(*) FROM siga_saas.ROLES_PERMISOS WHERE rol = 'ADMINISTRADOR'")
        count_admin = cur.fetchone()[0]
        print(f"   ✓ ROLES_PERMISOS (ADMINISTRADOR): {count_admin} permisos")
        
        cur.execute("SELECT COUNT(*) FROM siga_saas.ROLES_PERMISOS WHERE rol = 'OPERADOR'")
        count_operador = cur.fetchone()[0]
        print(f"   ✓ ROLES_PERMISOS (OPERADOR): {count_operador} permisos")
        
        cur.execute("SELECT COUNT(*) FROM siga_saas.ROLES_PERMISOS WHERE rol = 'CAJERO'")
        count_cajero = cur.fetchone()[0]
        print(f"   ✓ ROLES_PERMISOS (CAJERO): {count_cajero} permisos")
        
        cur.close()
        conn.close()
        
        print("\n✅ ¡Migración completada exitosamente!")
        print("   El sistema de permisos está listo para usar.")
        
        return True
        
    except psycopg2.Error as e:
        print(f"❌ Error de base de datos: {e}")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

if __name__ == '__main__':
    print("=" * 60)
    print("  MIGRACIÓN: Sistema de Permisos Granular")
    print("=" * 60)
    print()
    
    if ejecutar_migracion():
        exit(0)
    else:
        exit(1)
