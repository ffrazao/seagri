import { useState } from 'react';
import keycloak from './keycloak';
import api from './api';

function App() {
  const [mensagem, setMensagem] = useState(null);
  const [loading, setLoading] = useState(false);

  // ID da sua organização (Dona da Sede)
  const organizacaoId = 'a1a5f550-e6fe-4d2d-b9db-bf141b855ce7';
  // Substitua pelo ID da unidade "Sede SEAGRI DF" que criamos pelo terminal
  const unidadeId = '1cc5a191-71d5-4233-9bee-d407501bdbca'; 

  const handleRegistrar = () => {
    setLoading(true);
    setMensagem(null);

    // 1. Captura o GPS nativo do HTML5
    if (!navigator.geolocation) {
      setMensagem({ tipo: 'erro', texto: 'Geolocalização não é suportada pelo seu navegador.' });
      setLoading(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const payload = {
            unidadeId: unidadeId,
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            precisaoGps: position.coords.accuracy,
            dispositivoId: 'navegador-web', // Em um app real, geraríamos um UUID do dispositivo
            modoRegistro: 'SELF',
            capturadoEm: new Date().toISOString()
          };

          // 2. Envia para o nosso motor antifraude no backend
          const response = await api.post(`/orgs/${organizacaoId}/presencas`, payload);
          const presenca = response.data.payload;

          // 3. Interpreta a resposta administrativa (RFC-008 / RFC-0003)
          if (presenca.statusAdministrativo === 'VALIDO') {
            setMensagem({ 
              tipo: 'sucesso', 
              texto: 'Ponto registrado com sucesso! Você está dentro do local permitido.' 
            });
          } else {
            setMensagem({ 
              tipo: 'alerta', 
              texto: `Ponto registrado, porém classificado como PENDENTE (Risco: ${presenca.pontuacaoRisco}). Sujeito à análise da chefia.` 
            });
          }
        } catch (error) {
          setMensagem({ 
            tipo: 'erro', 
            texto: 'Erro na comunicação com o servidor: ' + (error.response?.data?.message || error.message) 
          });
        } finally {
          setLoading(false);
        }
      },
      (error) => {
        setMensagem({ tipo: 'erro', texto: 'Falha ao capturar o GPS. Permita o acesso à localização.' });
        setLoading(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  };

  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '600px', margin: 'auto' }}>
      <h1>🟢 SEAGRI - Presença Inteligente</h1>
      
      <div style={{ background: '#f0f0f0', padding: '15px', borderRadius: '8px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>Olá, <strong>{keycloak.tokenParsed?.name}</strong></span>
        <button onClick={() => keycloak.logout()} style={{ cursor: 'pointer', padding: '5px 10px' }}>Sair</button>
      </div>

      <div style={{ border: '1px solid #ccc', padding: '30px', borderRadius: '8px', textAlign: 'center' }}>
        <p style={{ marginBottom: '20px', color: '#555' }}>
          Clique no botão abaixo para capturar sua localização atual e registrar o evento.
        </p>

        {/* O botão único conforme a RFC-008 */}
        <button 
          onClick={handleRegistrar} 
          disabled={loading}
          style={{ 
            padding: '15px 40px', 
            fontSize: '18px', 
            cursor: loading ? 'not-allowed' : 'pointer', 
            background: loading ? '#ccc' : '#0056b3', 
            color: 'white', 
            border: 'none', 
            borderRadius: '50px',
            fontWeight: 'bold',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}
        >
          {loading ? 'Aguarde...' : 'Registrar'}
        </button>

        {/* Feedback visual dinâmico baseado no backend */}
        {mensagem && (
          <div style={{ 
            marginTop: '25px', 
            padding: '15px', 
            borderRadius: '8px',
            background: mensagem.tipo === 'sucesso' ? '#e6ffe6' : mensagem.tipo === 'alerta' ? '#fff3cd' : '#f8d7da',
            border: `1px solid ${mensagem.tipo === 'sucesso' ? 'green' : mensagem.tipo === 'alerta' ? '#ffeeba' : '#f5c6cb'}`,
            color: mensagem.tipo === 'sucesso' ? 'green' : mensagem.tipo === 'alerta' ? '#856404' : '#721c24'
          }}>
            <strong>{mensagem.texto}</strong>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;