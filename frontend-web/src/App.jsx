import { useState, useEffect } from 'react';
import keycloak from './keycloak';
import api from './api';

// ============================================================================
// COMPONENTE 1: CABEÇALHO DA APLICAÇÃO
// ============================================================================
function Header({ usuario }) {
  return (
    <div style={{ background: '#f0f0f0', padding: '15px', borderRadius: '8px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <span>Olá, <strong>{usuario}</strong></span>
      <button onClick={() => keycloak.logout()} style={{ cursor: 'pointer', padding: '5px 10px', background: '#dc3545', color: 'white', border: 'none', borderRadius: '4px' }}>Sair</button>
    </div>
  );
}

// ============================================================================
// COMPONENTE 2: PAINEL DE ONBOARDING (CONVITE)
// ============================================================================
function PainelConvite() {
  const [codigo, setCodigo] = useState('');
  const [mensagem, setMensagem] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleVincular = async () => {
    if (!codigo.trim()) return;
    setLoading(true);
    setMensagem(null);
    try {
      const response = await api.post(`/convites/${codigo}/aceitar`);
      setMensagem({ tipo: 'sucesso', texto: response.data.payload });
    } catch (error) {
      setMensagem({ tipo: 'erro', texto: 'Erro: ' + (error.response?.data?.message || error.message) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px', marginBottom: '30px', background: '#fafafa' }}>
      <h3>Possui um código de convite?</h3>
      <p style={{ color: '#666', fontSize: '14px', marginBottom: '10px' }}>Digite abaixo para se vincular a uma organização.</p>
      <div style={{ display: 'flex', gap: '10px' }}>
        <input 
          type="text" 
          placeholder="Ex: SEAGRI-TESTE"
          value={codigo}
          onChange={(e) => setCodigo(e.target.value)}
          style={{ padding: '10px', flex: 1, borderRadius: '4px', border: '1px solid #ccc' }}
        />
        <button 
          onClick={handleVincular} disabled={loading}
          style={{ padding: '10px 20px', cursor: loading ? 'not-allowed' : 'pointer', background: loading ? '#ccc' : '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold' }}
        >
          {loading ? 'Aguarde...' : 'Vincular'}
        </button>
      </div>
      {mensagem && (
        <p style={{ marginTop: '10px', color: mensagem.tipo === 'sucesso' ? 'green' : 'red', fontWeight: 'bold' }}>{mensagem.texto}</p>
      )}
    </div>
  );
}

// ============================================================================
// COMPONENTE 3: PAINEL DE REGISTRO DE PRESENÇA (GPS + MOTOR ANTIFRAUDE)
// ============================================================================
function PainelRegistro({ organizacaoId, unidadeId, onRegistroSucesso }) {
  const [mensagem, setMensagem] = useState(null);
  const [loading, setLoading] = useState(false);

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
            setMensagem({ tipo: 'sucesso', texto: 'Ponto registrado com sucesso! (Dentro do local)' });
          } else {
            setMensagem({ tipo: 'alerta', texto: `Ponto registrado (PENDENTE). Risco: ${presenca.pontuacaoRisco}. Sujeito à análise.` });
          }
          
          // Chama a função pai para atualizar a tabela
          if (onRegistroSucesso) onRegistroSucesso();

        } catch (error) {
          setMensagem({ tipo: 'erro', texto: 'Erro: ' + (error.response?.data?.message || error.message) });
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
    <div style={{ border: '1px solid #ccc', padding: '30px', borderRadius: '8px', textAlign: 'center', marginBottom: '30px' }}>
      <p style={{ marginBottom: '20px', color: '#555' }}>Clique no botão abaixo para capturar sua localização e registrar o evento.</p>
      <button 
        onClick={handleRegistrar} disabled={loading}
        style={{ padding: '15px 40px', fontSize: '18px', cursor: loading ? 'not-allowed' : 'pointer', background: loading ? '#ccc' : '#0056b3', color: 'white', border: 'none', borderRadius: '50px', fontWeight: 'bold' }}
      >
        {loading ? 'Processando GPS...' : 'Registrar'}
      </button>

      {mensagem && (
        <div style={{ marginTop: '25px', padding: '15px', borderRadius: '8px', background: mensagem.tipo === 'sucesso' ? '#e6ffe6' : mensagem.tipo === 'alerta' ? '#fff3cd' : '#f8d7da', color: mensagem.tipo === 'sucesso' ? 'green' : mensagem.tipo === 'alerta' ? '#856404' : '#721c24' }}>
          <strong>{mensagem.texto}</strong>
        </div>
      )}
    </div>
  );
}

// ============================================================================
// COMPONENTE 4: TABELA DE HISTÓRICO
// ============================================================================
function TabelaHistorico({ historico }) {
  return (
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
                <td style={{ padding: '10px', color: '#666' }}>{registro.statusTecnico}</td>
                <td style={{ padding: '10px' }}>
                  <span style={{ 
                    padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold',
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
  );
}

// ============================================================================
// COMPONENTE PRINCIPAL (ORQUESTRADOR)
// ============================================================================
export default function App() {
  const [historico, setHistorico] = useState([]);

  // IDs fixos para o MVP
  const organizacaoId = 'a1a5f550-e6fe-4d2d-b9db-bf141b855ce7';
  const unidadeId = '1cc5a191-71d5-4233-9bee-d407501bdbca'; 

  useEffect(() => {
    carregarHistorico();
  }, []);

  const carregarHistorico = async () => {
    try {
      const response = await api.get(`/orgs/${organizacaoId}/presencas`);
      setHistorico(response.data.payload || []);
    } catch (error) {
      console.error("Erro ao buscar histórico:", error);
    }
  };

  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '800px', margin: 'auto' }}>
      <h1>🟢 SEAGRI - Presença Inteligente</h1>
      
      <Header usuario={keycloak.tokenParsed?.name} />
      
      <PainelConvite />
      
      <PainelRegistro 
        organizacaoId={organizacaoId} 
        unidadeId={unidadeId} 
        onRegistroSucesso={carregarHistorico} 
      />
      
      <TabelaHistorico historico={historico} />
    </div>
  );
}