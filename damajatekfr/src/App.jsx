import './App.css'
import {Route, Router, Routes} from "react-router";
import Home from "./pages/Home.jsx";

function App() {

  return (
      <AuthProvider>
        <Router>
          <Navbar />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
                path="/protected"
                element={
                  <PrivateRoute>
                    <div>Protected Page</div>
                  </PrivateRoute>
                }
            />
          </Routes>
        </Router>
      </AuthProvider>
  );
}

export default App
