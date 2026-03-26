FROM postgres:16-alpine

# Instala dependências para suporte a locales e ICU
RUN apk add --no-cache \
    icu-data-full \
    icu-libs \
    musl-locales \
    musl-locales-lang

# Variáveis de ambiente de build para o banco
ENV LANG=pt_BR.UTF-8
ENV LANGUAGE=pt_BR.UTF-8
ENV LC_ALL=pt_BR.UTF-8

# O script oficial do Postgres usará isso no initdb
ENV POSTGRES_INITDB_ARGS="--locale-provider=icu --icu-locale=pt-BR"

# O Postgres oficial já expõe a 5432, mas deixamos explícito
EXPOSE 5432

# O comando padrão da imagem base já é 'postgres'
# As otimizações de log serão passadas via docker-compose ou postgresql.conf
