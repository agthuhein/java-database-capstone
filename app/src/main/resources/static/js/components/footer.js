function renderFooter() {
    const footer = document.getElementById("footer");
    if (!footer) return;

    footer.innerHTML = `
    <footer class="footer">
      <!-- Branding -->
      <div class="footer-logo">
        <img src="/assets/images/logo.png" alt="Smart Clinic Logo" />
        <p>© ${new Date().getFullYear()} Smart Clinic Management System</p>
      </div>

      <!-- Footer Links -->
      <div class="footer-links">
        <div class="footer-column">
          <h4>Company</h4>
          <a href="#">About</a>
          <a href="#">Careers</a>
          <a href="#">Press</a>
        </div>

        <div class="footer-column">
          <h4>Support</h4>
          <a href="#">Account</a>
          <a href="#">Help Center</a>
          <a href="#">Contact</a>
        </div>

        <div class="footer-column">
          <h4>Legals</h4>
          <a href="#">Terms</a>
          <a href="#">Privacy Policy</a>
          <a href="#">Licensing</a>
        </div>
      </div>
    </footer>
  `;
}

// Call the function so footer renders when script loads
document.addEventListener("DOMContentLoaded", renderFooter);

// Optional: expose for manual calls if needed
window.renderFooter = renderFooter;
