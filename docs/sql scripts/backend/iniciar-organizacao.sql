create extension if not exists dblink;

insert
	into
	public.organizacao
(id,
	nome,
	status,
	criado_por,
	criado_em)
select
	'11111111-1111-1111-1111-111111111111',
	'SEAGRI',
	'ATIVO',
	id,
	now()
from
	dblink(
    'dbname=keycloak host=localhost port=5432 user=keycloak password=uQ5&hS2^dX8tVw1J',
    'SELECT id AS id FROM public.user_entity WHERE username = ''abc'''
) as dblink(id varchar);

insert
	into
	public.unidade (
    id,
	organizacao_id,
	nome,
	criado_por,
	criado_em,
	tipo_geometria,
	centro_geo_lat,
	centro_geo_lng,
	raio_geo_metros,
	poligono_geo
)
select
	'11111111-1111-1111-1111-111111111112'::uuid,
	'11111111-1111-1111-1111-111111111111'::uuid,
	'DILOG',
	id::varchar,
	now(),
	'RAIO',
	-15.73102,
	-47.90331,
	150,
	null::jsonb
from
	dblink(
    'dbname=keycloak host=localhost port=5432 user=keycloak password=uQ5&hS2^dX8tVw1J',
    'SELECT id FROM public.user_entity WHERE username = ''abc'''
) as dblink(id uuid)
union all

select
	'22222222-2222-2222-2222-222222222222'::uuid,
	'11111111-1111-1111-1111-111111111111'::uuid,
	'GETI',
	id::varchar,
	now(),
	'RAIO',
	-15.73102,
	-47.90331,
	150,
	null::jsonb
from
	dblink(
    'dbname=keycloak host=localhost port=5432 user=keycloak password=uQ5&hS2^dX8tVw1J',
    'SELECT id FROM public.user_entity WHERE username = ''abc'''
) as dblink(id uuid);

insert
	into
	public.convite
(id,
	organizacao_id,
	unidade_id,
	papel_esperado,
	codigo,
	data_expiracao,
	usado,
	criado_por,
	criado_em)
select
	'33333333-3333-3333-3333-333333333333'::uuid,
	'11111111-1111-1111-1111-111111111111'::uuid,
	'11111111-1111-1111-1111-111111111112'::uuid,
	'PARTICIPANTE', 
	'CONV01',
	(now() + interval '10 days')::timestamp, 
	false, 
	id::varchar, 
	now()
from
	dblink(
    'dbname=keycloak host=localhost port=5432 user=keycloak password=uQ5&hS2^dX8tVw1J',
    'SELECT id FROM public.user_entity WHERE username = ''abc'''
) as dblink(id uuid)
union all

select
	'44444444-4444-4444-4444-444444444444'::uuid,
	'11111111-1111-1111-1111-111111111111'::uuid,
	'22222222-2222-2222-2222-222222222222'::uuid,
	'PARTICIPANTE', 
	'CONV02',
	(now() + interval '10 days')::timestamp, 
	false, 
	id::varchar, 
	now()
from
	dblink(
    'dbname=keycloak host=localhost port=5432 user=keycloak password=uQ5&hS2^dX8tVw1J',
    'SELECT id FROM public.user_entity WHERE username = ''abc'''
) as dblink(id uuid);
