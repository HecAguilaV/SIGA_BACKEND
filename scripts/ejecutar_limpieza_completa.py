#!/usr/bin/env python3
"""
Script para ejecutar la limpieza completa de la base de datos (excepto planes).
ADVERTENCIA: Esto elimina TODOS los datos (operativos y comerciales) excepto planes.

Ejecutar: python scripts/ejecutar_limpieza_completa.py
"""

import psycopg2
import sys
from pathlib import Path

# Configuración de la base de datos (AlwaysData)
DB_CONFIG = {
    'host': 'postgresql-hector.alwaysdata.net',
    'port': 5432,
    'dbname': 'hector_siga_db',
    'user': 'hector',
    'password': 'kike4466'  # ⚠️ Cambiar si es necesario
}

def ejecutar_limpieza():
    """Ejecuta el script de limpieza completa"""
    
    script_dir = Path(__file__).parent.parent
    sql_file = script_dir / 'src' / 'main' / 'resources' / 'db' / 'migrations' / '017_limpiar_todo_excepto_planes.sql'
    
    if not sql_file.exists():
        print(f"❌ Error: No se encontró el archivo {sql_file}")
        return False
    
    print("=" * 60)
    print("⚠️  ADVERTENCIA: Este script eliminará TODOS los datos")
    print("   (excepto planes)")
    print("=" * 60)
    
    # Permitir ejecución automática con --yes
    import sys
    if '--yes' not in sys.argv:
        respuesta = input("\n¿Estás seguro de que quieres continuar? (escribe 'SI' para confirmar): ")
        if respuesta != 'SI':
            print("❌ Operación cancelada")
            return False
    else:
        print("\n✅ Ejecutando automáticamente (--yes flag)")
    
    print(f"\n📄 Ejecutando: 017_limpiar_todo_excepto_planes.sql")
    
    try:
        # Conectar a la base de datos
        print("🔌 Conectando a la base de datos...")
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = False
        cursor = conn.cursor()
        
        print("✅ Conexión exitosa\n")
        
        # Leer el contenido del SQL
        with open(sql_file, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        
        # Ejecutar el script
        print("🗑️  Eliminando datos...")
        cursor.execute(sql_content)
        conn.commit()
        
        print("✅ Limpieza completada exitosamente\n")
        
        # Verificar resultados
        print("📊 Verificando resultados...")
        verificaciones = [
            ("Productos", "SELECT COUNT(*) FROM siga_saas.PRODUCTOS"),
            ("Locales", "SELECT COUNT(*) FROM siga_saas.LOCALES"),
            ("Usuarios Operativos", "SELECT COUNT(*) FROM siga_saas.USUARIOS"),
            ("Usuarios Comerciales", "SELECT COUNT(*) FROM siga_comercial.USUARIOS"),
            ("Suscripciones", "SELECT COUNT(*) FROM siga_comercial.SUSCRIPCIONES"),
            ("Facturas", "SELECT COUNT(*) FROM siga_comercial.FACTURAS"),
            ("Planes", "SELECT COUNT(*) FROM siga_comercial.PLANES")
        ]
        
        for nombre, query in verificaciones:
            cursor.execute(query)
            count = cursor.fetchone()[0]
            estado = "✅" if (nombre == "Planes" and count > 0) or (nombre != "Planes" and count == 0) else "⚠️"
            print(f"   {estado} {nombre}: {count}")
        
        cursor.close()
        conn.close()
        
        print("\n🎉 ¡Limpieza completada exitosamente!")
        print("\n📝 Próximos pasos:")
        print("   1. Registrar nuevo usuario en Web Comercial")
        print("   2. Crear suscripción (esto crea usuario operativo automáticamente)")
        print("   3. Hacer login en WebApp/App Móvil")
        print("   4. Crear productos, locales, etc. (todo con empresa asignada automáticamente)")
        
        return True
        
    except psycopg2.Error as e:
        if conn:
            conn.rollback()
        print(f"❌ Error al ejecutar limpieza:")
        print(f"   {e.pgcode}: {e.pgerror}")
        print(f"   {e}")
        return False
    except Exception as e:
        if conn:
            conn.rollback()
        print(f"❌ Error inesperado:")
        print(f"   {e}")
        return False

if __name__ == '__main__':
    print("=" * 60)
    print("🧹 Script de Limpieza Completa de Base de Datos")
    print("=" * 60)
    print()
    
    if ejecutar_limpieza():
        sys.exit(0)
    else:
        sys.exit(1)
