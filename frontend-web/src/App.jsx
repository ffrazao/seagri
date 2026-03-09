import { useState, useEffect } from 'react';
import keycloak from './keycloak';
import api from './api';

function App() {
  const [mensagem, setMensagem] = useState(null);
  const [loading, setLoading] = useState(false);
  const [historico, setHistorico] = useState([]); // Novo estado para a tabela

  // IDs fixos para o MVP
  const organizacaoId = 'a1a5f550-e6fe-4d2d-b9db-bf141b855ce7';
  const unidadeId = '1cc5a191-71d5-4233-9bee-d407501bdbca'; 

  // Carrega a lista de pontos assim que a tela abre
  useEffect(() => {
    carregarHistorico();
  }, []);

  const carregarHistorico = async () => {
    try {
      const response = await api.get(`/orgs/${organizacaoId}/presencas`);
      // Pega o payload da resposta padronizada (ApiResponse)
      setHistorico(response.data.payload || []);
    } catch (error) {
      console.error("Erro ao buscar histórico:", error);
    }
  };

  const handleRegistrar = () => {
    setLoading(true);
    setMensagem(null);

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
            dispositivoId: 'navegador-web',
            modoRegistro: 'SELF',
            capturadoEm: new Date().toISOString()
          };

          const response = await api.post(`/orgs/${organizacaoId}/presencas`, payload);
          const presenca = response.data.payload;

          if (presenca.statusAdministrativo === 'VALIDO') {
            setMensagem({ tipo: 'sucesso', texto: 'Ponto registrado com sucesso! Você está dentro do local permitido.' });
          } else {
            setMensagem({ tipo: 'alerta', texto: `Ponto registrado (PENDENTE). Risco: ${presenca.pontuacaoRisco}. Sujeito à análise da chefia.` });
          }
          
          // Atualiza a tabela imediatamente após registrar
          carregarHistorico();

        } catch (error) {
          setMensagem({ tipo: 'erro', texto: 'Erro na comunicação: ' + (error.response?.data?.message || error.message) });
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
    <div style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '800px', margin: 'auto' }}>
      <h1>🟢 SEAGRI - Presença Inteligente</h1>
      
      <div style={{ background: '#f0f0f0', padding: '15px', borderRadius: '8px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>Olá, <strong>{keycloak.tokenParsed?.name}</strong></span>
        <button onClick={() => keycloak.logout()} style={{ cursor: 'pointer', padding: '5px 10px' }}>Sair</button>
      </div>

      <div style={{ border: '1px solid #ccc', padding: '30px', borderRadius: '8px', textAlign: 'center', marginBottom: '30px' }}>
        <p style={{ marginBottom: '20px', color: '#555' }}>Clique no botão abaixo para capturar sua localização atual e registrar o evento.</p>
        <button 
          onClick={handleRegistrar} 
          disabled={loading}
          style={{ padding: '15px 40px', fontSize: '18px', cursor: loading ? 'not-allowed' : 'pointer', background: loading ? '#ccc' : '#0056b3', color: 'white', border: 'none', borderRadius: '50px', fontWeight: 'bold' }}
        >
          {loading ? 'Aguarde...' : 'Registrar'}
        </button>

        {mensagem && (
          <div style={{ marginTop: '25px', padding: '15px', borderRadius: '8px', background: mensagem.tipo === 'sucesso' ? '#e6ffe6' : mensagem.tipo === 'alerta' ? '#fff3cd' : '#f8d7da', color: mensagem.tipo === 'sucesso' ? 'green' : mensagem.tipo === 'alerta' ? '#856404' : '#721c24' }}>
            <strong>{mensagem.texto}</strong>
          </div>
        )}
      </div>

      {/* Tabela de Histórico */}
      <div style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px' }}>
        <h3>Seu Histórico Recente</h3>
        {historico.length === 0 ? (
          <p style={{ color: '#777' }}>Nenhum registro encontrado.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ padding: '10px' }}>Data / Hora</th>
                <th style={{ padding: '10px' }}>Status Técnico</th>
                <th style={{ padding: '10px' }}>Status Administrativo</th>
              </tr>
            </thead>
            <tbody>
              {historico.map((registro) => (
                <tr key={registro.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '10px' }}>{new Date(registro.capturadoEm).toLocaleString('pt-BR')}</td>
                  
                  {/* Status Técnico (RFC-008: Sempre RECEBIDO) */}
                  <td style={{ padding: '10px', color: '#666' }}>{registro.statusTecnico}</td>
                  
                  {/* Status Administrativo (RFC-0003: Avaliação Antifraude) */}
                  <td style={{ padding: '10px' }}>
                    <span style={{ 
                      padding: '4px 8px', 
                      borderRadius: '12px', 
                      fontSize: '12px', 
                      fontWeight: 'bold',
                      background: registro.statusAdministrativo === 'VALIDO' ? '#e6ffe6' : '#fff3cd',
                      color: registro.statusAdministrativo === 'VALIDO' ? 'green' : '#856404'
                    }}>
                      {registro.statusAdministrativo === 'VALIDO' ? '🟢 VÁLIDO' : '🟡 PENDENTE'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default App;