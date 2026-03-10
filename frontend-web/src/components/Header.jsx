import keycloak from '../keycloak';

export default function Header({ usuario }) {
  return (
    <div style={{ background: '#f0f0f0', padding: '15px', borderRadius: '8px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <span>Olá, <strong>{usuario}</strong></span>
      <button 
        onClick={() => keycloak.logout()} 
        style={{ cursor: 'pointer', padding: '5px 10px', background: '#dc3545', color: 'white', border: 'none', borderRadius: '4px' }}
      >
        Sair
      </button>
    </div>
  );
}