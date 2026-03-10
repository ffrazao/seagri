import { useState, useEffect, useRef } from 'react';
import api from '../api';

export default function PainelRegistro({ organizacaoId, unidadeId, onRegistroSucesso }) {
  const [mensagem, setMensagem] = useState(null);
  const [loading, setLoading] = useState(false);
  const [permissaoCamera, setPermissaoCamera] = useState(true);

  const videoRef = useRef(null);
  const canvasRef = useRef(null);

  useEffect(() => {
    iniciarCamera();
    return () => {
      if (videoRef.current && videoRef.current.srcObject) {
        videoRef.current.srcObject.getTracks().forEach(track => track.stop());
      }
    };
  }, []);

  const iniciarCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setPermissaoCamera(true);
    } catch (err) {
      console.warn("Câmera negada ou indisponível:", err);
      // Não bloqueamos a tela! Apenas avisamos o estado. (RFC-008)
      setPermissaoCamera(false); 
    }
  };

  // Função que tenta pegar o GPS, mas devolve null pacificamente se o usuário negar
  const obterLocalizacao = () => {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        resolve({ latitude: null, longitude: null, precisaoGps: null });
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          precisaoGps: pos.coords.accuracy
        }),
        (err) => {
          console.warn("GPS negado ou indisponível:", err);
          resolve({ latitude: null, longitude: null, precisaoGps: null });
        },
        { timeout: 5000, enableHighAccuracy: true }
      );
    });
  };

  const capturarFoto = () => {
    if (!permissaoCamera || !videoRef.current || !videoRef.current.srcObject) {
      return null;
    }
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');
    canvas.width = videoRef.current.videoWidth;
    canvas.height = videoRef.current.videoHeight;
    context.drawImage(videoRef.current, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL('image/jpeg', 0.8);
  };

  const handleRegistrar = async () => {
    setLoading(true);
    setMensagem(null);

    try {
      // 1. Coleta o que for possível (Foto e GPS)
      const fotoBase64 = capturarFoto();
      const coords = await obterLocalizacao();

      // 2. Monta o DTO (se negado, enviaremos null, e o Backend julgará o risco)
      const payload = {
        unidadeId: unidadeId,
        latitude: coords.latitude,
        longitude: coords.longitude,
        precisaoGps: coords.precisaoGps,
        dispositivoId: navigator.userAgent.substring(0, 128),
        modoRegistro: 'SELF',
        capturadoEm: new Date().toISOString(),
        fotoBase64: fotoBase64
      };

      await api.post(`/orgs/${organizacaoId}/presencas`, payload);

      setMensagem({ tipo: 'sucesso', texto: 'Presença registrada! (Sujeita a auditoria)' });
      if (onRegistroSucesso) onRegistroSucesso();

    } catch (error) {
      console.error("Erro ao registrar:", error);
      setMensagem({ tipo: 'erro', texto: 'Erro de comunicação com o servidor.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ border: '1px solid #ccc', padding: '30px', borderRadius: '8px', textAlign: 'center', marginBottom: '30px', background: '#fff' }}>
      <p style={{ marginBottom: '20px', color: '#555', fontWeight: 'bold' }}>
        Posicione seu rosto na câmera e clique em Registrar.
      </p>

      {/* Exibição condicional da câmera */}
      <div style={{ marginBottom: '20px' }}>
        {permissaoCamera ? (
          <video ref={videoRef} autoPlay playsInline style={{ width: '100%', maxWidth: '300px', borderRadius: '8px', transform: 'scaleX(-1)' }} />
        ) : (
          <div style={{ width: '100%', maxWidth: '300px', height: '225px', background: '#ffebee', border: '1px dashed #c62828', margin: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px', color: '#c62828', fontWeight: 'bold' }}>
            📸 Câmera Negada
          </div>
        )}
        <canvas ref={canvasRef} style={{ display: 'none' }} />
      </div>

      {mensagem && (
        <div style={{ padding: '10px', marginBottom: '15px', borderRadius: '4px', background: mensagem.tipo === 'erro' ? '#ffebee' : '#e8f5e9', color: mensagem.tipo === 'erro' ? '#c62828' : '#2e7d32' }}>
          {mensagem.texto}
        </div>
      )}

      <button
        onClick={handleRegistrar}
        disabled={loading}
        style={{ background: '#0056b3', color: '#fff', border: 'none', padding: '10px 20px', fontSize: '16px', borderRadius: '4px', cursor: loading ? 'not-allowed' : 'pointer' }}
      >
        {loading ? 'Enviando Registro...' : 'Registrar Presença'}
      </button>
    </div>
  );
}