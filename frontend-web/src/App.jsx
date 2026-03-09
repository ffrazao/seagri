import { useState } from 'react';
import keycloak from './keycloak';
import api from './api';

function App() {
  const [mensagem, setMensagem] = useState('');
  const [loading, setLoading] = useState(false);

  const testarCriacaoOrganizacao = async () => {
    setLoading(true);
    try {
      // Dispara a requisição para o backend que construímos na Fase 2
      const response = await api.post('/orgs', { nome: 'Fazenda Criada pelo React' });
      setMensagem(`Sucesso! Organização criada com ID: ${response.data.payload.id}`);
    } catch (error) {
      setMensagem('Erro: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '600px', margin: 'auto' }}>
      <h1>🟢 SEAGRI Plataforma</h1>
      
      <div style={{ background: '#f0f0f0', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
        {/* Lendo o nome do usuário de dentro do Token JWT */}
        <p>Logado como: <strong>{keycloak.tokenParsed?.name}</strong></p>
        <button onClick={() => keycloak.logout()} style={{ cursor: 'pointer' }}>Sair (Logout)</button>
      </div>

      <div style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px' }}>
        <h2>Painel do MVP</h2>
        <p>Para testarmos a conexão da API com a segurança contextual, tente criar uma organização:</p>
        <button 
          onClick={testarCriacaoOrganizacao} 
          disabled={loading}
          style={{ padding: '10px 20px', cursor: 'pointer', background: '#0056b3', color: 'white', border: 'none', borderRadius: '4px' }}
        >
          {loading ? 'Criando...' : 'Criar Organização Teste'}
        </button>

        {mensagem && (
          <p style={{ marginTop: '20px', padding: '10px', background: '#e6ffe6', borderLeft: '4px solid green' }}>
            {mensagem}
          </p>
        )}
      </div>
    </div>
  );
}

export default App;