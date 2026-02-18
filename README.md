# SEAGRI - Infraestrutura de Identidade e Dados

Este repositório contém o blueprint de arquitetura para a fundação de microserviços da SEAGRI. A solução utiliza **Keycloak** para Gestão de Identidade (IAM) e **PostgreSQL** como camada de persistência robusta, tudo orquestrado via Docker.

## 🏗️ Arquitetura do Stack

A infraestrutura foi desenhada seguindo princípios de isolamento de processos e persistência de dados:

* **Identity Provider:** Keycloak 26.0 (baseado em Quarkus) configurado em modo de desenvolvimento.
* **Database:** PostgreSQL 16 (Alpine) com volumes segregados para dados e logs.
* **Segurança:** Separação de roles entre Superusuário (postgres) e Usuário de Aplicação (keycloak_admin).



---

## 🚀 Como Iniciar

### Pré-requisitos
* Docker e Docker Compose instalados.
* Portas `8080` e `5432` liberadas no host (Ubuntu).

### Instalação e Execução
1. Clone o repositório:
   ```bash
   git clone ffrazao_github:ffrazao/seagri.git
   cd seagri


Execute o script de provisionamento (ele configurará permissões de pasta e credenciais):

Bash
chmod +x setup.sh
./setup.sh
Inicie o stack:

Bash
docker-compose up -d
Acompanhe a subida dos serviços:

Bash
docker-compose logs -f
🛠️ Detalhes Técnicos de Implementação
Flexibilidade de Configuração
Diferente de implementações estáticas, este projeto utiliza um Entrypoint Shell Script (init-db.sh) para o PostgreSQL. Isso permite que qualquer alteração no arquivo .env seja refletida dinamicamente no banco de dados durante a inicialização, sem a necessidade de recriar scripts manuais.

Persistência e Monitoramento
Dados: Persistidos em ./postgres/data.

Logs: Mapeados para ./postgres/logs e ./keycloak/logs, facilitando a depuração externa sem entrar nos containers.

Acesso Administrativo
Keycloak Admin Console: http://localhost:8080/admin (Credenciais no .env)

Database (DBeaver/psql): Host localhost, Porta 5432.

🛡️ Segurança: Usuário Admin Temporário
Ao acessar o Keycloak pela primeira vez, um aviso de segurança sobre "usuário temporário" será exibido.
Recomendação: Crie um novo usuário administrativo através do painel Users e atribua a Realm Role admin a ele para desativar o bootstrap inicial.

Desenvolvido por [ffrazao] como parte da arquitetura de backend SEAGRI.
