/// <reference types="vite/client" />

declare module '*.svg' {
  const content: string;
  export default content;
}

declare module '*.svg?react' {
  import { FC, SVGProps } from 'react';
  const content: FC<SVGProps<SVGSVGElement>>;
  export default content;
}
```

**Solución 2: Verificar que el archivo existe**

Asegúrate de que el archivo existe en la ruta correcta:
```
src/assets/icons/sentinel-logo.svg