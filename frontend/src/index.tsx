import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import Map from './components/map/map';
import LoginForm from './components/auth/LoginForm';
import { RouterProvider, Link } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import router, { LINKS } from './router/router';
import Heading from './components/base/Heading';
import NavLink from './components/base/NavLink';


const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  // <React.StrictMode>

  <div className='w-full'>
    <header className="flex flex-row justify-between items-center sm:w-11/12 mx-auto">
      <Heading>Demo</Heading>
      <nav className='flex flex-row justify-evenly gap-x-3 items-center'>
        <NavLink link={LINKS.DASHBOARD}>Dashboard</NavLink>
        <NavLink link={LINKS.LOGIN}>Sign In</NavLink>
        <NavLink link={LINKS.REGISTER}>Sign Up</NavLink>
        <NavLink link={LINKS.GROUPS}>Groups</NavLink>
      </nav>
    </header>
    <RouterProvider router={router} />
  </div>
  
    // <Map/>
  // <LoginForm />
  // <Input id='login' type='email' name='email' placeholder='name@company.com' required/>

  // </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
