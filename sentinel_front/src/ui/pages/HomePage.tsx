import React from 'react';
import { useState, useEffect  } from 'react';
import { useTheme } from '../../utils/store/themeContext';
import { useTranslation } from 'react-i18next';
import { Crosshair } from 'lucide-react';
import Sentinel from '@/components/commons/Sentinel';
import Footer from '@/components/commons/Footer';
import { Navbar } from '@/components/commons/Navbar';


export default function HomePage() {

  const API_URL = import.meta.env.VITE_API_URL as string;
  const { theme} = useTheme();
  const { i18n, t } = useTranslation();

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'es' ? 'en' : 'es');
  };




  return (
    <>

    <Navbar />
      {/* Hero Section */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gridTemplateRows:'1fr 1fr',
        gap: '60px',
        padding: '80px 40px',
        alignItems: 'center',
        maxWidth: '1700px',
        minHeight:'100vh',
        backgroundPosition:'center',
        backgroundSize:'cover',
        margin: '0 auto',
        backgroundImage: `url('/sentinel_landing_page-bg.jpg')`,
      }}>
        <div>
          <h1 style={{
            fontSize: '72px',
            fontWeight: 'bold',
            lineHeight: '1.2',
            marginBottom: '20px',
            backgroundImage: `linear-gradient(135deg, ${theme.colors.text.primary} 0%, ${theme.colors.primaryLight} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            backgroundColor: 'transparent'
          }}>
            {t('hero.title')}
          </h1>
          
          <p style={{
            fontSize: '16px',
            color: theme.colors.text.secondary,
            lineHeight: '1.6',
            marginBottom: '40px',
            maxWidth: '500px'
          }}>
            {t('hero.description')}
          </p>

          <button style={{
            backgroundColor: theme.colors.primary,
            color: '#fff',
            border: 'none',
            padding: '16px 40px',
            borderRadius: '50px',
            fontSize: '16px',
            fontWeight: 'bold',
            cursor: 'pointer',
            letterSpacing: '1px',
            transition: 'all 0.3s ease',
            boxShadow: `0 0 20px ${theme.colors.primary}40`
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = theme.colors.primaryDark;
            e.currentTarget.style.transform = 'scale(1.05)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = theme.colors.primary;
            e.currentTarget.style.transform = 'scale(1)';
          }}>
            {t('hero.cta')}
          </button>
        </div>
        

      {/* Services Section */}
      <div style={{
        gridArea: '1 / 2 / span 2',
        height:'100%',
        display: 'flex',
        flexDirection:'column',
        justifyContent:'flex-end'


      }}>
      <div style={{
        padding: '80px 40px',
        maxWidth: '1400px',
        margin: '0 auto'
      }}>
        <h3 style={{
          fontSize: '48px',
          fontWeight: 'bold',
          textAlign: 'center',
          marginBottom: '20px'
        }}>
          {t('services.title.part1')} <span style={{ color: theme.colors.primary }}>{t('services.title.highlight')}</span>
          <br />
          <span style={{ color: theme.colors.primary }}>{t('services.title.part2')}</span> {t('services.title.part3')}
        </h3>

      </div>
      </div>
      </section>
      <section>
        <div style={{
  gridColumn: '1 / -1',
  padding: '80px 40px',
  textAlign: 'center'
}}>
<Sentinel />

  {/* Título */}
  <h3 style={{
    fontSize: '56px',
    fontWeight: 'bold',
    lineHeight: '1.2',
    marginBottom: '60px'
  }}>
    <span style={{ color: theme.colors.text.primary }}>{t('services.title.part1')} </span>
    <span style={{ color: theme.colors.primary }}>{t('services.title.highlight')}</span>
    <br />
    <span style={{ color: theme.colors.text.primary }}>{t('services.title.part2')}</span>
  </h3>

  {/* Grid de servicios */}
<div
  style={{
    display: 'grid',
    gridTemplateColumns: 'repeat(4, 1fr)',
    gap: '20px',
    maxWidth: '1200px',
    margin: '0 auto'
  }}
>
  {[
    { key: 'content', icon:  <Crosshair /> },
    { key: 'social', icon:  <Crosshair /> },
    { key: 'analytics', icon:  <Crosshair />  },
    { key: 'advertising', icon:  <Crosshair /> },
  ].map((service, idx) => (
    <div
      key={idx}
      style={{
        padding: '40px 30px',
        borderRadius: '32px',
        backgroundColor: '#fff',
        color: theme.colors.secondaryDark,
        textAlign: 'center',
        transition: 'all 0.3s ease',
        cursor: 'pointer',
        border: `2px solid ${theme.colors.border}`,
        minHeight: '220px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}
  onMouseEnter={(e) => {
  const el = e.currentTarget;
  el.style.backgroundColor = theme.colors.primary;
  el.style.borderColor = theme.colors.primary;
  el.style.color = '#fff';
  el.style.transform = 'translateY(-8px)';
  
  el.style.overflow = 'visible';

  const icon = el.querySelector('.hover-icon') as HTMLElement;
  if (icon) icon.style.opacity = '1'; // ⬅️ FULL VISIBLE
}}

onMouseLeave={(e) => {
  const el = e.currentTarget;
  el.style.backgroundColor = '#fff';
  el.style.borderColor = theme.colors.border;
  el.style.color = theme.colors.secondaryDark;
  el.style.transform = 'translateY(0)';
  
  el.style.overflow = 'hidden';

  const icon = el.querySelector('.hover-icon') as HTMLElement;
  if (icon) icon.style.opacity = '0'; // ⬅️ OCULTO
}}

    >
      {/* Icono decorativo aparece solo en hover */}
      <div
        className="hover-icon"
        style={{
          position: 'absolute',
          top: '-110px',
          right: '-20px',
          fontSize: '120px',
          color: theme.colors.primaryDark,

          opacity: 0,
          transform: 'rotate(15deg)',
          transition: 'opacity 0.3s ease',
          pointerEvents: 'none',
        }}
      >  {React.cloneElement(service.icon, {
    size: 50,
    strokeWidth: 1.5,
  })}
      </div>

      {/* Título */}
      <div
        style={{
          fontSize: '18px',
          fontWeight: 'bold',
          letterSpacing: '0.5px',
          lineHeight: '1.5',
          whiteSpace: 'pre-line',
          position: 'relative',
          zIndex: 1,
        }}
      >
        {t(`services.items.${service.key}`)}
      </div>

      {/* Descripción */}
      <p
        style={{
          fontSize: '12px',
          marginTop: '12px',
          opacity: 0.9,
          lineHeight: '1.4',
          zIndex: 1,
        }}
      >
        {t(`services.items.${service.key}.description`)}
      </p>
    </div>
  ))}
</div>


</div>
      </section>
      


<Footer/>
      
    </>
  );
}