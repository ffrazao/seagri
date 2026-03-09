import Keycloak from 'keycloak-js';

// Configuração apontando para o seu Keycloak local e o realm corporativo
const keycloakConfig = {
  url: 'http://localhost:8080',
  realm: 'corporativo', 
  clientId: 'seagri-web'
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;