const root = document.getElementById("root");

root.innerHTML = `
    <div class="navbar">
        <div class="logo">🔐 SecureVault</div>

        <div class="nav-links">
            <a href="#">Home</a>
            <a href="#features">Features</a>
            <a href="#">About</a>
            <button class="login-btn" onclick="window.location.href='login.html'">
    Login
</button>
        </div>
    </div>

    <section class="hero">

        <div class="hero-content">

            <h1>
                Your Passwords.<br>
                <span>Your Security.</span>
            </h1>

            <p>
                SecureVault is a secure password and credential
                management system designed to protect and organize
                your sensitive information.
            </p>

            <div class="buttons">
                <button class="primary-btn" onclick="window.location.href='login.html'">
    Get Started
</button>
            <button class="secondary-btn" onclick="document.querySelector('.features').scrollIntoView({behavior: 'smooth'})">
    Learn More
</button>
            </div>

        </div>

        <div class="hero-card">

            <div class="lock-icon">🔐</div>

            <h2>Secure Vault</h2>

            <p>
                AES-256 Encryption
            </p>

            <p>
                🛡️ Your credentials are protected
            </p>

        </div>

    </section>

    <section class="features" id="features">

        <h2>Why SecureVault?</h2>

        <div class="feature-container">

            <div class="feature-card">
                <h3>🔒 Secure Storage</h3>
                <p>
                    Store your credentials securely with
                    encrypted storage.
                </p>
            </div>

            <div class="feature-card">
                <h3>🔑 Password Generator</h3>
                <p>
                    Generate strong and secure passwords easily.
                </p>
            </div>

            <div class="feature-card">
                <h3>🤝 Secure Sharing</h3>
                <p>
                    Share credentials with controlled permissions
                    and expiry.
                </p>
            </div>

        </div>

    </section>

    <footer>
        <p>© 2026 SecureVault | Secure Credential Management</p>
    </footer>
`;