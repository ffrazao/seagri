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


Aqui está o conteúdo formatado em **Markdown**, pronto para ser copiado e colado no seu arquivo `README.md`. Ele utiliza a sintaxe padrão do GitHub para garantir que tabelas, blocos de código e destaques fiquem visualmente organizados.

---

```markdown
# 🛡️ Guia de Engenharia Reversa e Federação de Identidade (AD/LDAP + Keycloak)

Este guia documenta o processo de descoberta de infraestrutura de rede e a configuração de um ambiente de autenticação corporativa resiliente, focado no cenário do **GDF (Governo do Distrito Federal)**.

---

## 📋 Parte 1: Descoberta de Infraestrutura (Ubuntu/Linux)

O objetivo desta etapa é identificar os parâmetros do Active Directory (AD) sem depender de documentação prévia.

### 1.1 Localização do Domínio e Servidores
O servidor de rede (DHCP) sempre envia o domínio para o cliente. Use o `nmcli` para extraí-lo:

```bash
# Identifica o domínio oficial da rede
nmcli dev show | grep "DOMAIN\|DNS"

```

* **Esperado:** O campo `IP4.DOMAIN` revelará o sufixo (ex: `governo.gdfnet.df`).

### 1.2 Identificação dos Domain Controllers (DCs)

Consulte o DNS para listar os servidores que prestam serviço LDAP no domínio encontrado:

```bash
# Lista os servidores e portas
dig -t SRV _ldap._tcp.governo.gdfnet.df

```

### 1.3 Validação do Catálogo Global (Porta 3268)

Para autenticação em larga escala (múltiplos órgãos), confirme se o Catálogo Global está acessível:

```bash
# Valida se a porta 3268 está aberta no servidor alvo
nc -zv 10.194.250.111 3268

```

### 1.4 Extração do seu Distinguished Name (DN)

O LDAP exige o caminho completo do objeto. Use seu login para descobrir seu DN exato:

```bash
ldapsearch -H ldap://10.194.250.111 -x \
  -D "seu_usuario@governo.gdfnet.df" -W \
  -b "DC=governo,DC=gdfnet,DC=df" \
  "(sAMAccountName=seu_usuario)" dn

```

---

## ⚙️ Parte 2: Configuração do Keycloak (Realm Corporativo)

Configuração estratégica para evitar redundância de dados e tratar múltiplas contas com o mesmo e-mail.

### 2.1 Configurações do Realm `corporativo`

Para suportar usuários com o mesmo e-mail (ex: contas `admin` e `comum`), ajuste em **Realm Settings > Login**:

* **Email as username:** `Off`
* **Login with email:** `Off` (Obrigatório para evitar ambiguidade).
* **Duplicate emails:** `On` (Permitido).

### 2.2 Federação LDAP (User Federation)

Adicione um provedor LDAP com os parâmetros abaixo:

| Campo | Valor Sugerido |
| --- | --- |
| **Console Display Name** | `AD-GDF-Global` |
| **Connection URL** | `ldap://governo.gdfnet.df:3268` |
| **Bind DN** | `ldap@governo.gdfnet.df` |
| **Bind Credential** | `Geti1247890*` |
| **Users DN** | `OU=UNIDADES,DC=governo,DC=gdfnet,DC=df` |
| **Edit Mode** | `READ_ONLY` |
| **Import Users** | `Off` (Garante que nenhum dado seja salvo no banco do Keycloak) |

### 2.3 Mapeamento de Identidade e Grupos

Para garantir que o login seja feito pelo CPF/Matrícula e que os grupos do AD virem Roles no Java:

1. **Username Mapper:** Atributo LDAP `sAMAccountName`.
2. **Groups Mapper:**
* **Mapper Type:** `group-ldap-mapper`
* **LDAP Groups DN:** `OU=UNIDADES,DC=governo,DC=gdfnet,DC=df`
* **Mapped Group Attribute:** `cn`



---

## 🏗️ Arquitetura da Solução

* **Autenticação em Tempo Real:** O Keycloak valida as credenciais contra o AD e mantém os dados apenas em memória (Stateless).
* **Resiliência:** O uso do FQDN (`governo.gdfnet.df`) permite que o DNS realize Round Robin entre os 4 servidores disponíveis.
* **Tratamento de Colisão:** A unicidade é garantida pelo `sAMAccountName`, permitindo que o mesmo servidor possua múltiplos perfis com o mesmo e-mail sem conflitos no Keycloak.

---

*Documentação gerada para suporte a arquiteturas de Back-End Java/Spring.*


Desenvolvido por [ffrazao] como parte da arquitetura de backend SEAGRI.





