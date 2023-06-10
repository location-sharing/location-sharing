import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import './index.css';
import reportWebVitals from './reportWebVitals';
import router from './router/router';


const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  // <React.StrictMode>

  
    <RouterProvider router={router} />
  
    // <Map/>
  // <LoginForm />
  // <Input id='login' type='email' name='email' placeholder='name@company.com' required/>

  // </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
