import Script from "next/script";

interface AnalyticsConfig {
  ga_tracking_id?: string;
  ga_code?: string;
  gtm_id?: string;
  custom_head?: string;
}

interface Props {
  configJson?: string;
}

export default function AnalyticsScripts({ configJson }: Props) {
  if (!configJson) return null;

  let config: AnalyticsConfig = {};
  try {
    config = JSON.parse(configJson);
  } catch {
    return null;
  }

  const gaId = config.ga_tracking_id?.trim();

  return (
    <>
      {/* Google Analytics (gtag) */}
      {gaId && (
        <>
          <Script
            src={`https://www.googletagmanager.com/gtag/js?id=${gaId}`}
            strategy="afterInteractive"
          />
          <Script
            id="ga-init"
            strategy="afterInteractive"
            dangerouslySetInnerHTML={{
              __html: `
                window.dataLayer = window.dataLayer || [];
                function gtag(){dataLayer.push(arguments);}
                gtag('js', new Date());
                gtag('config', '${gaId}', { page_path: window.location.pathname });
              `,
            }}
          />
        </>
      )}

      {/* GA 自定义代码降级 */}
      {!gaId && config.ga_code?.trim() && (
        <Script
          id="ga-custom"
          strategy="afterInteractive"
          dangerouslySetInnerHTML={{ __html: config.ga_code.trim() }}
        />
      )}

      {/* Google Tag Manager */}
      {config.gtm_id?.trim() && (
        <Script
          id="gtm-head"
          strategy="afterInteractive"
          dangerouslySetInnerHTML={{
            __html: `
              (function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
              new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
              j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
              'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
              })(window,document,'script','dataLayer','${config.gtm_id.trim()}');
            `,
          }}
        />
      )}

      {/* 自定义 Head 代码 */}
      {config.custom_head?.trim() && (
        <Script
          id="custom-head"
          strategy="afterInteractive"
          dangerouslySetInnerHTML={{ __html: config.custom_head.trim() }}
        />
      )}
    </>
  );
}
