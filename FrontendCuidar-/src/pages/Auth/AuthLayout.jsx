import BcLogo from "../../components/Bclogo/BcLogo";

export default function AuthLayout({ children, eyebrow, title, description, highlights }) {
  return (
    <main className="login-page">
      <section className="login-page__hero">
        <div className="login-page__hero-content">
          <div className="login-page__eyebrow">{eyebrow}</div>
          <BcLogo size="lg" />
          <h1>{title}</h1>
          <p>{description}</p>
          <div className="login-page__highlights" aria-label="Diferenciais da plataforma">
            {highlights.map((highlight) => (
              <article className="login-highlight" key={highlight.title}>
                <span className="login-highlight__icon" aria-hidden="true">{highlight.icon}</span>
                <div>
                  <strong>{highlight.title}</strong>
                  <p>{highlight.description}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="login-page__panel" aria-label="Formulario de autenticacao">
        {children}
      </section>
    </main>
  );
}
