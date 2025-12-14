#!/usr/bin/env python3
"""
Script para ejecutar las migraciones de separación por empresa.
Ejecutar: python scripts/ejecutar_migraciones_empresa.py
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

def ejecutar_migraciones():
    """Ejecuta las migraciones 013 y 014"""
    
    script_dir = Path(__file__).parent.parent
    
    migraciones = [
        '013_add_usuario_comercial_id.sql',
        '014_separacion_completa_por_empresa.sql'
    ]
    
    try:
        # Conectar a la base de datos
        print("🔌 Conectando a la base de datos...")
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = False
        cursor = conn.cursor()
        
        print("✅ Conexión exitosa\n")
        
        for migracion in migraciones:
            sql_file = script_dir / 'src' / 'main' / 'resources' / 'db' / 'migrations' / migracion
            
            if not sql_file.exists():
                print(f"❌ Error: No se encontró el archivo {sql_file}")
                return False
            
            print(f"📄 Ejecutando: {migracion}")
            
            try:
                # Leer el contenido del SQL
                with open(sql_file, 'r', encoding='utf-8') as f:
                    sql_content = f.read()
                
                # Ejecutar el script
                cursor.execute(sql_content)
                conn.commit()
                
                print(f"✅ {migracion} ejecutado exitosamente\n")
                
            except psycopg2.Error as e:
                conn.rollback()
                print(f"❌ Error al ejecutar {migracion}:")
                print(f"   {e.pgcode}: {e.pgerror}")
                print(f"   {e}")
                return False
            except Exception as e:
                conn.rollback()
                print(f"❌ Error inesperado al ejecutar {migracion}:")
                print(f"   {e}")
                return False
        
        cursor.close()
        conn.close()
        
        print("🎉 ¡Todas las migraciones ejecutadas exitosamente!")
        return True
        
    except psycopg2.Error as e:
        print(f"❌ Error de conexión a la base de datos:")
        print(f"   {e.pgcode}: {e.pgerror}")
        print(f"   {e}")
        return False
    except Exception as e:
        print(f"❌ Error inesperado:")
        print(f"   {e}")
        return False

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 EJECUTANDO MIGRACIONES DE SEPARACIÓN POR EMPRESA")
    print("=" * 60)
    print()
    
    if ejecutar_migraciones():
        print()
        print("=" * 60)
        print("✅ PROCESO COMPLETADO")
        print("=" * 60)
        exit(0)
    else:
        print()
        print("=" * 60)
        print("❌ PROCESO FALLIDO")
        print("=" * 60)
        exit(1)
