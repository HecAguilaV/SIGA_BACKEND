--
-- PostgreSQL database dump
--

\restrict Eyf9tLgDqzFkagYXIMXqRN3OdGYLShkZTAMIH27DBEagCcynTBMP3tKwQo4cMSY

-- Dumped from database version 16.13
-- Dumped by pg_dump version 17.8

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: siga_comercial; Type: SCHEMA; Schema: -; Owner: hector
--

CREATE SCHEMA siga_comercial;


ALTER SCHEMA siga_comercial OWNER TO hector;

--
-- Name: SCHEMA siga_comercial; Type: COMMENT; Schema: -; Owner: hector
--

COMMENT ON SCHEMA siga_comercial IS 'Portal comercial y gestión de suscripciones';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: carritos; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.carritos (
    id integer NOT NULL,
    usuario_id integer,
    plan_id integer,
    periodo character varying(20) DEFAULT 'MENSUAL'::character varying NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT carritos_periodo_check CHECK (((periodo)::text = ANY ((ARRAY['MENSUAL'::character varying, 'ANUAL'::character varying])::text[])))
);


ALTER TABLE siga_comercial.carritos OWNER TO hector;

--
-- Name: TABLE carritos; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.carritos IS 'Carritos de compra de planes';


--
-- Name: carritos_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.carritos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.carritos_id_seq OWNER TO hector;

--
-- Name: carritos_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.carritos_id_seq OWNED BY siga_comercial.carritos.id;


--
-- Name: facturas; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.facturas (
    id integer NOT NULL,
    suscripcion_id integer,
    pago_id integer,
    numero_factura character varying(50) NOT NULL,
    usuario_id integer NOT NULL,
    usuario_nombre character varying(255) NOT NULL,
    usuario_email character varying(255) NOT NULL,
    plan_id integer NOT NULL,
    plan_nombre character varying(255) NOT NULL,
    precio_uf numeric(10,2) NOT NULL,
    precio_clp numeric(12,2),
    unidad character varying(10) DEFAULT 'UF'::character varying NOT NULL,
    fecha_compra timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_vencimiento timestamp without time zone,
    estado character varying(20) DEFAULT 'PAGADA'::character varying NOT NULL,
    metodo_pago character varying(100),
    ultimos_4_digitos character varying(4),
    iva numeric(10,2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT facturas_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'PAGADA'::character varying, 'VENCIDA'::character varying, 'ANULADA'::character varying])::text[]))),
    CONSTRAINT facturas_iva_check CHECK ((iva >= (0)::numeric)),
    CONSTRAINT facturas_precio_clp_check CHECK ((precio_clp >= (0)::numeric)),
    CONSTRAINT facturas_precio_uf_check CHECK ((precio_uf >= (0)::numeric))
);


ALTER TABLE siga_comercial.facturas OWNER TO hector;

--
-- Name: TABLE facturas; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.facturas IS 'Facturas generadas para suscripciones';


--
-- Name: facturas_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.facturas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.facturas_id_seq OWNER TO hector;

--
-- Name: facturas_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.facturas_id_seq OWNED BY siga_comercial.facturas.id;


--
-- Name: pagos; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.pagos (
    id integer NOT NULL,
    suscripcion_id integer,
    monto numeric(10,2) NOT NULL,
    moneda character varying(10) DEFAULT 'CLP'::character varying,
    metodo_pago character varying(50),
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    referencia_externa character varying(255),
    fecha_pago timestamp without time zone,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pagos_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'COMPLETADO'::character varying, 'FALLIDO'::character varying, 'REEMBOLSADO'::character varying])::text[]))),
    CONSTRAINT pagos_monto_check CHECK ((monto >= (0)::numeric))
);


ALTER TABLE siga_comercial.pagos OWNER TO hector;

--
-- Name: TABLE pagos; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.pagos IS 'Registro de pagos de suscripciones';


--
-- Name: pagos_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.pagos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.pagos_id_seq OWNER TO hector;

--
-- Name: pagos_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.pagos_id_seq OWNED BY siga_comercial.pagos.id;


--
-- Name: planes; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.planes (
    id integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    precio_mensual numeric(10,2) NOT NULL,
    precio_anual numeric(10,2),
    limite_bodegas integer DEFAULT 1,
    limite_usuarios integer DEFAULT 1,
    limite_productos integer,
    caracteristicas jsonb,
    activo boolean DEFAULT true,
    orden integer DEFAULT 0,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT planes_limite_bodegas_check CHECK ((limite_bodegas > 0)),
    CONSTRAINT planes_limite_usuarios_check CHECK ((limite_usuarios > 0)),
    CONSTRAINT planes_precio_anual_check CHECK ((precio_anual >= (0)::numeric)),
    CONSTRAINT planes_precio_mensual_check CHECK ((precio_mensual >= (0)::numeric))
);


ALTER TABLE siga_comercial.planes OWNER TO hector;

--
-- Name: TABLE planes; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.planes IS 'Planes de suscripción disponibles';


--
-- Name: COLUMN planes.caracteristicas; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.planes.caracteristicas IS 'JSON con características del plan: {"trial_gratis": true, "soporte": "email", etc}';


--
-- Name: planes_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.planes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.planes_id_seq OWNER TO hector;

--
-- Name: planes_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.planes_id_seq OWNED BY siga_comercial.planes.id;


--
-- Name: suscripciones; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.suscripciones (
    id integer NOT NULL,
    usuario_id integer,
    plan_id integer,
    fecha_inicio date NOT NULL,
    fecha_fin date,
    estado character varying(20) DEFAULT 'ACTIVA'::character varying NOT NULL,
    periodo character varying(20) DEFAULT 'MENSUAL'::character varying NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT suscripciones_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVA'::character varying, 'SUSPENDIDA'::character varying, 'CANCELADA'::character varying, 'VENCIDA'::character varying])::text[]))),
    CONSTRAINT suscripciones_periodo_check CHECK (((periodo)::text = ANY ((ARRAY['MENSUAL'::character varying, 'ANUAL'::character varying])::text[])))
);


ALTER TABLE siga_comercial.suscripciones OWNER TO hector;

--
-- Name: TABLE suscripciones; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.suscripciones IS 'Suscripciones activas de clientes';


--
-- Name: suscripciones_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.suscripciones_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.suscripciones_id_seq OWNER TO hector;

--
-- Name: suscripciones_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.suscripciones_id_seq OWNED BY siga_comercial.suscripciones.id;


--
-- Name: usuarios; Type: TABLE; Schema: siga_comercial; Owner: hector
--

CREATE TABLE siga_comercial.usuarios (
    id integer NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100),
    rut character varying(20),
    telefono character varying(20),
    activo boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    en_trial boolean DEFAULT false,
    fecha_inicio_trial timestamp without time zone,
    fecha_fin_trial timestamp without time zone,
    rol character varying(20) DEFAULT 'cliente'::character varying,
    plan_id integer,
    nombre_empresa character varying(255),
    CONSTRAINT usuarios_rol_check CHECK (((rol)::text = ANY ((ARRAY['admin'::character varying, 'cliente'::character varying])::text[])))
);


ALTER TABLE siga_comercial.usuarios OWNER TO hector;

--
-- Name: TABLE usuarios; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON TABLE siga_comercial.usuarios IS 'Clientes del portal comercial';


--
-- Name: COLUMN usuarios.en_trial; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.usuarios.en_trial IS 'Indica si el usuario está en período de trial';


--
-- Name: COLUMN usuarios.fecha_inicio_trial; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.usuarios.fecha_inicio_trial IS 'Fecha de inicio del trial (14 días)';


--
-- Name: COLUMN usuarios.fecha_fin_trial; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.usuarios.fecha_fin_trial IS 'Fecha de fin del trial';


--
-- Name: COLUMN usuarios.rol; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.usuarios.rol IS 'Rol del usuario: admin o cliente';


--
-- Name: COLUMN usuarios.plan_id; Type: COMMENT; Schema: siga_comercial; Owner: hector
--

COMMENT ON COLUMN siga_comercial.usuarios.plan_id IS 'ID del plan actual (cache, se sincroniza con suscripción activa)';


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: siga_comercial; Owner: hector
--

CREATE SEQUENCE siga_comercial.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE siga_comercial.usuarios_id_seq OWNER TO hector;

--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: siga_comercial; Owner: hector
--

ALTER SEQUENCE siga_comercial.usuarios_id_seq OWNED BY siga_comercial.usuarios.id;


--
-- Name: carritos id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.carritos ALTER COLUMN id SET DEFAULT nextval('siga_comercial.carritos_id_seq'::regclass);


--
-- Name: facturas id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas ALTER COLUMN id SET DEFAULT nextval('siga_comercial.facturas_id_seq'::regclass);


--
-- Name: pagos id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.pagos ALTER COLUMN id SET DEFAULT nextval('siga_comercial.pagos_id_seq'::regclass);


--
-- Name: planes id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.planes ALTER COLUMN id SET DEFAULT nextval('siga_comercial.planes_id_seq'::regclass);


--
-- Name: suscripciones id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.suscripciones ALTER COLUMN id SET DEFAULT nextval('siga_comercial.suscripciones_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.usuarios ALTER COLUMN id SET DEFAULT nextval('siga_comercial.usuarios_id_seq'::regclass);


--
-- Name: carritos carritos_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.carritos
    ADD CONSTRAINT carritos_pkey PRIMARY KEY (id);


--
-- Name: carritos carritos_usuario_id_key; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.carritos
    ADD CONSTRAINT carritos_usuario_id_key UNIQUE (usuario_id);


--
-- Name: facturas facturas_numero_factura_key; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_numero_factura_key UNIQUE (numero_factura);


--
-- Name: facturas facturas_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_pkey PRIMARY KEY (id);


--
-- Name: pagos pagos_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.pagos
    ADD CONSTRAINT pagos_pkey PRIMARY KEY (id);


--
-- Name: planes planes_nombre_key; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.planes
    ADD CONSTRAINT planes_nombre_key UNIQUE (nombre);


--
-- Name: planes planes_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.planes
    ADD CONSTRAINT planes_pkey PRIMARY KEY (id);


--
-- Name: suscripciones suscripciones_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.suscripciones
    ADD CONSTRAINT suscripciones_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_carritos_usuario; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_carritos_usuario ON siga_comercial.carritos USING btree (usuario_id);


--
-- Name: idx_facturas_estado; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_facturas_estado ON siga_comercial.facturas USING btree (estado);


--
-- Name: idx_facturas_pago; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_facturas_pago ON siga_comercial.facturas USING btree (pago_id);


--
-- Name: idx_facturas_plan_id; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_facturas_plan_id ON siga_comercial.facturas USING btree (plan_id);


--
-- Name: idx_facturas_suscripcion; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_facturas_suscripcion ON siga_comercial.facturas USING btree (suscripcion_id);


--
-- Name: idx_facturas_usuario_id; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_facturas_usuario_id ON siga_comercial.facturas USING btree (usuario_id);


--
-- Name: idx_pagos_estado; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_pagos_estado ON siga_comercial.pagos USING btree (estado);


--
-- Name: idx_pagos_fecha; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_pagos_fecha ON siga_comercial.pagos USING btree (fecha_pago);


--
-- Name: idx_pagos_suscripcion; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_pagos_suscripcion ON siga_comercial.pagos USING btree (suscripcion_id);


--
-- Name: idx_suscripciones_estado; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_suscripciones_estado ON siga_comercial.suscripciones USING btree (estado);


--
-- Name: idx_suscripciones_plan; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_suscripciones_plan ON siga_comercial.suscripciones USING btree (plan_id);


--
-- Name: idx_suscripciones_usuario; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_suscripciones_usuario ON siga_comercial.suscripciones USING btree (usuario_id);


--
-- Name: idx_usuarios_plan_id; Type: INDEX; Schema: siga_comercial; Owner: hector
--

CREATE INDEX idx_usuarios_plan_id ON siga_comercial.usuarios USING btree (plan_id);


--
-- Name: carritos carritos_plan_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.carritos
    ADD CONSTRAINT carritos_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES siga_comercial.planes(id);


--
-- Name: carritos carritos_usuario_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.carritos
    ADD CONSTRAINT carritos_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES siga_comercial.usuarios(id) ON DELETE CASCADE;


--
-- Name: facturas facturas_pago_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_pago_id_fkey FOREIGN KEY (pago_id) REFERENCES siga_comercial.pagos(id) ON DELETE SET NULL;


--
-- Name: facturas facturas_plan_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES siga_comercial.planes(id);


--
-- Name: facturas facturas_suscripcion_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_suscripcion_id_fkey FOREIGN KEY (suscripcion_id) REFERENCES siga_comercial.suscripciones(id) ON DELETE SET NULL;


--
-- Name: facturas facturas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.facturas
    ADD CONSTRAINT facturas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES siga_comercial.usuarios(id) ON DELETE CASCADE;


--
-- Name: pagos pagos_suscripcion_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.pagos
    ADD CONSTRAINT pagos_suscripcion_id_fkey FOREIGN KEY (suscripcion_id) REFERENCES siga_comercial.suscripciones(id) ON DELETE CASCADE;


--
-- Name: suscripciones suscripciones_plan_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.suscripciones
    ADD CONSTRAINT suscripciones_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES siga_comercial.planes(id);


--
-- Name: suscripciones suscripciones_usuario_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.suscripciones
    ADD CONSTRAINT suscripciones_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES siga_comercial.usuarios(id) ON DELETE CASCADE;


--
-- Name: usuarios usuarios_plan_id_fkey; Type: FK CONSTRAINT; Schema: siga_comercial; Owner: hector
--

ALTER TABLE ONLY siga_comercial.usuarios
    ADD CONSTRAINT usuarios_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES siga_comercial.planes(id);


--
-- PostgreSQL database dump complete
--

\unrestrict Eyf9tLgDqzFkagYXIMXqRN3OdGYLShkZTAMIH27DBEagCcynTBMP3tKwQo4cMSY

