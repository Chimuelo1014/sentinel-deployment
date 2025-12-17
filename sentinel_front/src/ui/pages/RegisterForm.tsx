import { motion } from "framer-motion";
import { slideFadeRight } from "./animations";

export default function RegisterForm({ onSwitch }: { onSwitch: () => void }) {
  return (
    <motion.div 
      key="register"
      variants={slideFadeRight}
      initial="hidden"
      animate="show"
      className="form-wrapper"
      style={{ width: "100%" }}
    >
      <h1 className="title">Create account</h1>

      <div className="input-box">
        <i className="fas fa-user"></i>
        <input type="text" placeholder="Username"/>
      </div>

      <div className="input-box">
        <i className="fas fa-envelope"></i>
        <input type="email" placeholder="Email"/>
      </div>

      <div className="input-box">
        <i className="fas fa-lock"></i>
        <input type="password" placeholder="Password"/>
      </div>

      <button className="button is-primary" style={{ width: "100%", borderRadius: 12 }}>
        Register
      </button>

      <p style={{ marginTop: 25, opacity: 0.7 }}>or register with social platforms</p>

      <div className="social-row">
        <button className="social-btn">G</button>
        <button className="social-btn">f</button>
        <button className="social-btn">in</button>
        <button className="social-btn">{"{"}{"}"}</button>
      </div>

      <p style={{ marginTop: 30 }}>
        Already have an account?
        <span style={{ cursor: "pointer", color: "#712bda" }} onClick={onSwitch}>
          {" "}Login
        </span>
      </p>
    </motion.div>
  );
}
