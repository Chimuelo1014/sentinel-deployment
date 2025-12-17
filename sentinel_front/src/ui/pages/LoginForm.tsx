import { motion } from "framer-motion";
import { slideFade } from "./animations";

export default function LoginForm({ onSwitch }: { onSwitch: () => void }) {
  return (
    <motion.div 
      key="login"
      variants={slideFade}
      initial="hidden"
      animate="show"
      className="form-wrapper"
      style={{ width: "100%" }}
    >
      <h1 className="title">Log in</h1>

      <div className="input-box">
        <i className="fas fa-user"></i>
        <input type="text" placeholder="Username"/>
      </div>

      <div className="input-box">
        <i className="fas fa-lock"></i>
        <input type="password" placeholder="Password"/>
      </div>

      <p style={{ fontSize: 13, marginBottom: 15, cursor: "pointer" }}>
        Forgot password?
      </p>

      <button className="button is-primary" style={{ width: "100%", borderRadius: 12 }}>
        Login
      </button>

      <p style={{ marginTop: 25, opacity: 0.7 }}>or login with social platforms</p>

      <div className="social-row">
        <button className="social-btn">G</button>
        <button className="social-btn">f</button>
        <button className="social-btn">in</button>
        <button className="social-btn">{"{"}{"}"}</button>
      </div>

      <p style={{ marginTop: 30 }}>
        Don’t have an account?
        <span style={{ cursor: "pointer", color: "#712bda" }} onClick={onSwitch}>
          {" "}Register
        </span>
      </p>
    </motion.div>
  );
}
