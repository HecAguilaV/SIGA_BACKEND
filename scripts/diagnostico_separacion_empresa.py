#!/usr/bin/env python3
"""
Script de diagnóstico para verificar separación por empresa
Verifica que los usuarios tengan usuario_comercial_id asignado y que los datos estén correctamente separados
"""

import psycopg2
import os
from typing import Dict, List, Tuple

# Configuración de base de datos (AlwaysData)
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'postgresql-hectoraguilav.alwaysdata.net'),
    'database': os.getenv('DB_NAME', 'hectoraguilav_siga'),
    'user': os.getenv('DB_USER', 'hectoraguilav_siga'),
    'password': os.getenv('DB_PASSWORD', 'SIGA2025!')
}

def conectar_db():
    """Conecta a la base de datos"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"❌ Error al conectar: {e}")
        return None

def diagnosticar_usuarios(conn) -> List[Dict]:
    """Diagnostica usuarios operativos y su usuario_comercial_id"""
    print("\n" + "="*60)
    print("📊 DIAGNÓSTICO DE USUARIOS OPERATIVOS")
    print("="*60)
    
    cursor = conn.cursor()
    
    # Obtener todos los usuarios operativos
    cursor.execute("""
        SELECT u.id, u.email, u.usuario_comercial_id, u.rol, u.activo
        FROM siga_saas.USUARIOS u
        ORDER BY u.id
    """)
    
    usuarios = []
    for row in cursor.fetchall():
        usuario_id, email, usuario_comercial_id, rol, activo = row
        usuarios.append({
            'id': usuario_id,
            'email': email,
            'usuario_comercial_id': usuario_comercial_id,
            'rol': rol,
            'activo': activo
        })
        
        status = "✅" if usuario_comercial_id else "❌"
        print(f"{status} Usuario ID {usuario_id}: {email}")
        print(f"   Rol: {rol}, Activo: {activo}")
        print(f"   usuario_comercial_id: {usuario_comercial_id or 'NULL (PROBLEMA!)'}")
        
        # Verificar si existe usuario comercial con este email
        if not usuario_comercial_id:
            cursor.execute("""
                SELECT id, nombre, email
                FROM siga_comercial.USUARIOS
                WHERE LOWER(email) = LOWER(%s)
            """, (email,))
            comercial = cursor.fetchone()
            if comercial:
                print(f"   ⚠️  Existe usuario comercial ID {comercial[0]} con este email pero NO está vinculado")
            else:
                print(f"   ⚠️  NO existe usuario comercial con este email")
        print()
    
    return usuarios

def diagnosticar_productos(conn, usuarios: List[Dict]):
    """Diagnostica productos y su separación por empresa"""
    print("\n" + "="*60)
    print("📦 DIAGNÓSTICO DE PRODUCTOS")
    print("="*60)
    
    cursor = conn.cursor()
    
    # Agrupar por usuario_comercial_id
    cursor.execute("""
        SELECT 
            usuario_comercial_id,
            COUNT(*) as total,
            COUNT(CASE WHEN activo = true THEN 1 END) as activos,
            COUNT(CASE WHEN activo = false THEN 1 END) as inactivos
        FROM siga_saas.PRODUCTOS
        GROUP BY usuario_comercial_id
        ORDER BY usuario_comercial_id NULLS LAST
    """)
    
    print("\nProductos por empresa:")
    for row in cursor.fetchall():
        usuario_comercial_id, total, activos, inactivos = row
        if usuario_comercial_id is None:
            print(f"❌ NULL (sin empresa): {total} total ({activos} activos, {inactivos} inactivos)")
        else:
            print(f"✅ Empresa ID {usuario_comercial_id}: {total} total ({activos} activos, {inactivos} inactivos)")
    
    # Mostrar productos sin empresa
    cursor.execute("""
        SELECT id, nombre, precio_unitario, activo
        FROM siga_saas.PRODUCTOS
        WHERE usuario_comercial_id IS NULL
        LIMIT 10
    """)
    
    productos_sin_empresa = cursor.fetchall()
    if productos_sin_empresa:
        print(f"\n⚠️  Productos SIN empresa asignada (primeros 10):")
        for prod_id, nombre, precio, activo in productos_sin_empresa:
            print(f"   ID {prod_id}: {nombre} - Precio: {precio} - Activo: {activo}")

def diagnosticar_locales(conn):
    """Diagnostica locales y su separación por empresa"""
    print("\n" + "="*60)
    print("🏪 DIAGNÓSTICO DE LOCALES")
    print("="*60)
    
    cursor = conn.cursor()
    
    # Agrupar por usuario_comercial_id
    cursor.execute("""
        SELECT 
            usuario_comercial_id,
            COUNT(*) as total,
            COUNT(CASE WHEN activo = true THEN 1 END) as activos
        FROM siga_saas.LOCALES
        GROUP BY usuario_comercial_id
        ORDER BY usuario_comercial_id NULLS LAST
    """)
    
    print("\nLocales por empresa:")
    for row in cursor.fetchall():
        usuario_comercial_id, total, activos = row
        if usuario_comercial_id is None:
            print(f"❌ NULL (sin empresa): {total} total ({activos} activos)")
        else:
            print(f"✅ Empresa ID {usuario_comercial_id}: {total} total ({activos} activos)")
    
    # Mostrar locales sin empresa
    cursor.execute("""
        SELECT id, nombre, ciudad, activo
        FROM siga_saas.LOCALES
        WHERE usuario_comercial_id IS NULL
    """)
    
    locales_sin_empresa = cursor.fetchall()
    if locales_sin_empresa:
        print(f"\n⚠️  Locales SIN empresa asignada:")
        for loc_id, nombre, ciudad, activo in locales_sin_empresa:
            print(f"   ID {loc_id}: {nombre} ({ciudad}) - Activo: {activo}")

def diagnosticar_stock(conn):
    """Diagnostica stock y verifica consistencia"""
    print("\n" + "="*60)
    print("📊 DIAGNÓSTICO DE STOCK")
    print("="*60)
    
    cursor = conn.cursor()
    
    # Verificar stock con productos/locales de diferentes empresas
    cursor.execute("""
        SELECT 
            s.id,
            s.producto_id,
            s.local_id,
            p.usuario_comercial_id as producto_empresa,
            l.usuario_comercial_id as local_empresa
        FROM siga_saas.STOCK s
        JOIN siga_saas.PRODUCTOS p ON s.producto_id = p.id
        JOIN siga_saas.LOCALES l ON s.local_id = l.id
        WHERE p.usuario_comercial_id != l.usuario_comercial_id
           OR p.usuario_comercial_id IS NULL
           OR l.usuario_comercial_id IS NULL
        LIMIT 10
    """)
    
    stock_inconsistente = cursor.fetchall()
    if stock_inconsistente:
        print(f"\n⚠️  Stock INCONSISTENTE (producto y local de diferentes empresas):")
        for row in stock_inconsistente:
            s_id, prod_id, loc_id, prod_emp, loc_emp = row
            print(f"   Stock ID {s_id}: Producto {prod_id} (empresa {prod_emp}) vs Local {loc_id} (empresa {loc_emp})")
    else:
        print("\n✅ Stock consistente: todos los productos y locales pertenecen a la misma empresa")

def generar_recomendaciones(conn, usuarios: List[Dict]):
    """Genera recomendaciones basadas en el diagnóstico"""
    print("\n" + "="*60)
    print("💡 RECOMENDACIONES")
    print("="*60)
    
    cursor = conn.cursor()
    
    # Verificar usuarios sin usuario_comercial_id
    usuarios_sin_empresa = [u for u in usuarios if u['usuario_comercial_id'] is None]
    if usuarios_sin_empresa:
        print(f"\n❌ PROBLEMA CRÍTICO: {len(usuarios_sin_empresa)} usuarios sin empresa asignada:")
        for u in usuarios_sin_empresa:
            print(f"   - Usuario ID {u['id']}: {u['email']}")
            # Intentar encontrar usuario comercial
            cursor.execute("""
                SELECT id FROM siga_comercial.USUARIOS
                WHERE LOWER(email) = LOWER(%s)
            """, (u['email'],))
            comercial = cursor.fetchone()
            if comercial:
                print(f"     → SOLUCIÓN: Ejecutar: UPDATE siga_saas.USUARIOS SET usuario_comercial_id = {comercial[0]} WHERE id = {u['id']}")
            else:
                print(f"     → SOLUCIÓN: Crear usuario comercial primero o asignar manualmente")
    
    # Verificar productos sin empresa
    cursor.execute("SELECT COUNT(*) FROM siga_saas.PRODUCTOS WHERE usuario_comercial_id IS NULL")
    productos_sin_empresa = cursor.fetchone()[0]
    if productos_sin_empresa > 0:
        print(f"\n❌ PROBLEMA: {productos_sin_empresa} productos sin empresa asignada")
        print("   → SOLUCIÓN: Ejecutar migración 015_asignar_empresas_datos_existentes.sql")
    
    # Verificar locales sin empresa
    cursor.execute("SELECT COUNT(*) FROM siga_saas.LOCALES WHERE usuario_comercial_id IS NULL")
    locales_sin_empresa = cursor.fetchone()[0]
    if locales_sin_empresa > 0:
        print(f"\n❌ PROBLEMA: {locales_sin_empresa} locales sin empresa asignada")
        print("   → SOLUCIÓN: Ejecutar migración 015_asignar_empresas_datos_existentes.sql")

def main():
    """Función principal"""
    print("🔍 DIAGNÓSTICO DE SEPARACIÓN POR EMPRESA")
    print("="*60)
    
    conn = conectar_db()
    if not conn:
        return
    
    try:
        usuarios = diagnosticar_usuarios(conn)
        diagnosticar_productos(conn, usuarios)
        diagnosticar_locales(conn)
        diagnosticar_stock(conn)
        generar_recomendaciones(conn, usuarios)
        
        print("\n" + "="*60)
        print("✅ DIAGNÓSTICO COMPLETADO")
        print("="*60)
        
    except Exception as e:
        print(f"\n❌ Error durante diagnóstico: {e}")
        import traceback
        traceback.print_exc()
    finally:
        conn.close()

if __name__ == "__main__":
    main()
