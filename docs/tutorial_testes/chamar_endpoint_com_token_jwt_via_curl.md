

Copie o valor do campo access_token retornado no passo anterior e cole no lugar de SEU_TOKEN_AQUI no comando abaixo. Substitua também os UUIDs por IDs reais que existam no seu banco de dados para organizacao_id e unidade_id.
curl -i -X POST "http://localhost:8081/api/v1/presencas" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI" \
     -H "Content-Type: application/json" \
     -d '{
           "organizacaoId": "123e4567-e89b-12d3-a456-426614174000",
           "unidadeId": "123e4567-e89b-12d3-a456-426614174001",
           "latitude": -15.7942,
           "longitude": -47.8821,
           "precisaoGps": 12.5,
           "dispositivoId": "moto-g-7-XYZ123",
           "modoRegistro": "SELF",
           "capturadoEm": "2026-03-06T15:30:00"
         }'
O que acontece nos bastidores?
A requisição bate na porta 8081.
O Spring Security intercepta, lê o cabeçalho Authorization e valida a assinatura criptográfica do token usando a chave pública do Keycloak.
Se válido, ele extrai o seu ID de usuário (sub) e repassa ao nosso RegistroPresencaController.
O evento bruto é salvo no PostgreSQL como imutável, consolidando o comando único.
Resposta Esperada (HTTP 201 Created): O envelopamento padrão ApiResponse que criamos agirá aqui!
{
  "timestamp": "2026-03-06T18:35:00.123Z",
  "status": 201,
  "message": "Recurso criado com sucesso",
  "payload": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "usuarioId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "statusTecnico": "RECEIVED",
    "statusAdministrativo": "PENDING",
    "recebidoNoServidorEm": "2026-03-06T18:35:00.123"
  },
  "errors": null
}


Exemplo: 
curl -i -X POST "http://localhost:8081/api/v1/presencas" \
     -H "Authorization: Bearer " \
     -H "Content-Type: application/json" \
     -d '{
           "organizacaoId": "0e533b2f-9542-458e-8495-94f699d22805",
           "unidadeId": "1beca265-61c6-4212-91ce-ae7b525218ed",
           "latitude": -15.7942,
           "longitude": -47.8821,
           "precisaoGps": 12.5,
           "dispositivoId": "moto-g-7-XYZ123",
           "modoRegistro": "SELF",
           "capturadoEm": "2026-03-06T15:30:00"
         }'