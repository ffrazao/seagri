import { useState, useEffect } from 'react';
import keycloak from './keycloak';
import api from './api';

// Importando os componentes refatorados
import Header from './components/Header';
import PainelConvite from './components/PainelConvite';
import PainelRegistro from './components/PainelRegistro';
import TabelaHistorico from './components/TabelaHistorico';

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