import { useEffect, useState } from 'react';
import { Badge, Burger, Button, Paper } from '@mantine/core';
import { NavLink, Route, Routes, useLocation } from 'react-router-dom';
import { ArrowRight, ChartNoAxesCombined, CircleHelp, Database, FlaskConical, Layers3, LayoutDashboard, LineChart, PanelTop, Workflow } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { InstrumentsPage } from './features/instruments/InstrumentsPage';

const modules = [
  { path: '/simulations', name: 'Symulacje', icon: FlaskConical, description: 'Przestrzeń do porównywania wyników i analizy zachowania portfela w czasie.' },
  { path: '/strategies', name: 'Strategie', icon: Workflow, description: 'Parametry strategii inwestycyjnych i przegląd podejmowanych przez nie decyzji.' },
  { path: '/instruments', name: 'Instrumenty', icon: Layers3, description: 'Lista instrumentów, giełd i walut wykorzystywanych w Twoich analizach.' },
  { path: '/quotations', name: 'Notowania', icon: LineChart, description: 'Import oraz przegląd historycznych notowań instrumentów.' },
  { path: '/corporate-actions', name: 'Zdarzenia korporacyjne', icon: Database, description: 'Dywidendy i splity uwzględniane przy analizie danych historycznych.' },
];

function Placeholder({ name, description, icon: Icon }: { name: string; description: string; icon: LucideIcon }) {
  return <>
    <div className="page-heading"><span className="eyebrow">PRZESTRZEŃ ROBOCZA</span><h1>{name}</h1><p>{description}</p></div>
    <Paper className="empty-state" withBorder>
      <div className="empty-icon"><Icon size={30} strokeWidth={1.5} /></div>
      <Badge variant="light" color="gray">W przygotowaniu</Badge>
      <h2>Tu pojawi się moduł „{name}”</h2>
      <p>Układ aplikacji jest gotowy. Funkcje i dane dodamy w kolejnym etapie.</p>
      <Button component={NavLink} to="/" variant="light" color="teal">Wróć do przeglądu</Button>
    </Paper>
  </>;
}

function Overview() {
  return <>
    <div className="page-heading"><span className="eyebrow">TWOJA PRZESTRZEŃ ANALITYCZNA</span><h1>Spójrz na portfel z perspektywy.</h1><p>Od danych historycznych do świadomie dobranej strategii.</p></div>
    <Paper className="welcome-panel">
      <div><span className="eyebrow">PORTFOLIO MANAGER</span><h2>Miejsce na Twoje<br />strategie i analizy.</h2><p>Jedna przestrzeń do pracy z instrumentami,<br className="desktop-break" /> notowaniami i symulacjami portfela.</p><Button component={NavLink} to="/simulations" color="teal" rightSection={<ArrowRight size={17} />}>Przejdź do symulacji</Button></div>
      <div className="hero-symbol" aria-hidden="true"><ChartNoAxesCombined size={154} strokeWidth={0.85} /></div>
    </Paper>
    <div className="section-heading"><div><span className="eyebrow">MODUŁY</span><h2>Twoje narzędzia</h2></div><span className="section-note">Początek Twojego procesu analizy</span></div>
    <div className="module-grid">{modules.map(({ path, name, icon: Icon, description }, index) =>
      <NavLink className="module-card" to={path} key={path}>
        <div className="card-top"><span className="module-icon"><Icon size={22} strokeWidth={1.6} /></span><span className="module-number">0{index + 1}</span></div>
        <h3>{name}</h3><p>{description}</p><div className="card-bottom"><span>{path === '/instruments' ? 'Lista i dodawanie' : 'W przygotowaniu'}</span><ArrowRight size={17} /></div>
      </NavLink>,
    )}</div>
    <div className="scope-note"><PanelTop size={18} /><p>Zarządzaj instrumentami w module „Instrumenty”. Kolejne narzędzia analizy są w przygotowaniu.</p></div>
  </>;
}

export function App() {
  const [menuOpened, setMenuOpened] = useState(false);
  const location = useLocation();
  const currentName = location.pathname === '/' ? 'Przegląd' : modules.find((module) => module.path === location.pathname)?.name ?? 'Nie znaleziono strony';
  useEffect(() => { setMenuOpened(false); document.title = `${currentName} · Portfolio Manager`; }, [location.pathname, currentName]);

  return <div className="app-layout">
    <a className="skip-link" href="#main-content">Przejdź do treści</a>
    {menuOpened && <button className="menu-backdrop" aria-label="Zamknij menu" onClick={() => setMenuOpened(false)} />}
    <aside id="main-navigation" className={`sidebar ${menuOpened ? 'sidebar-open' : ''}`}>
      <NavLink to="/" className="brand"><span className="brand-mark"><ChartNoAxesCombined size={24} /></span><span>Portfolio<span className="brand-subtitle">MANAGER</span></span></NavLink>
      <div className="workspace-label">Osobista przestrzeń <span>PM</span></div>
      <nav aria-label="Menu główne"><span className="nav-label">PRZEGLĄD</span><NavLink to="/" end className="nav-item"><LayoutDashboard size={19} />Przegląd</NavLink><span className="nav-label analysis-label">ANALIZA I DANE</span>{modules.map(({ path, name, icon: Icon }) => <NavLink to={path} className="nav-item" key={path}><Icon size={19} />{name}</NavLink>)}</nav>
      <div className="sidebar-footer"><CircleHelp size={18} /><div>Przestrzeń do podejmowania<br />lepszych decyzji.</div></div>
    </aside>
    <div className="workspace">
      <header className="topbar"><div className="breadcrumb"><Burger opened={menuOpened} onClick={() => setMenuOpened((opened) => !opened)} aria-label={menuOpened ? 'Zamknij menu' : 'Otwórz menu'} aria-expanded={menuOpened} aria-controls="main-navigation" size="sm" className="mobile-menu" /><span>Portfolio Manager</span><span className="breadcrumb-divider">/</span><strong>{currentName}</strong></div><Badge variant="outline" color="gray" className="stage-badge">Wersja wstępna</Badge></header>
      <main id="main-content" tabIndex={-1}><Routes><Route path="/" element={<Overview />} />{modules.map((module) => <Route key={module.path} path={module.path} element={module.path === '/instruments' ? <InstrumentsPage /> : <Placeholder {...module} />} />)}<Route path="*" element={<Placeholder name="Nie znaleziono strony" description="Ten adres nie odpowiada żadnemu modułowi aplikacji." icon={CircleHelp} />} /></Routes></main>
      <footer className="page-footer"><span>Portfolio Manager</span><span>Twoje dane. Twoja perspektywa.</span></footer>
    </div>
  </div>;
}
