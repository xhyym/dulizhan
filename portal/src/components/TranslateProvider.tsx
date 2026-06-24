"use client";

import { useEffect } from "react";

declare global {
  interface Window {
    translate: any;
    translateInit: boolean;
  }
}

export default function TranslateProvider() {
  useEffect(() => {
    if (window.translateInit) return;
    window.translateInit = true;

    const script = document.createElement("script");
    script.src = "https://cdn.staticfile.net/translate.js/4.0.0/translate.min.js";
    script.onload = () => {
      if (!window.translate) return;
      window.translate.language.setLocal("english");
      window.translate.setAutoDiscriminateLocalLanguage();
      window.translate.selectLanguageTag.show = false;
      window.translate.service.use("client.edge");
      window.translate.listener.start();
      window.translate.execute();
    };
    document.head.appendChild(script);
  }, []);

  return null;
}
