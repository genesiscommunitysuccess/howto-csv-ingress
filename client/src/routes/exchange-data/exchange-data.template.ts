import { isDev } from '@genesislcap/foundation-utils';
import { html } from '@genesislcap/web-core';
import type { ExchangeData } from './exchange-data';
import { ExchangeDataExchangeHolidaysGrid } from './exchange-holidays-grid';

ExchangeDataExchangeHolidaysGrid;

export const ExchangeDataTemplate = html<ExchangeData>`
  <rapid-layout auto-save-key="${() => (isDev() ? null : 'Exchange Data_1722864326611')}">
     <rapid-layout-region>
         <rapid-layout-item title="Exchange Holidays">
             <exchange-data-exchange-holidays-grid></exchange-data-exchange-holidays-grid>
         </rapid-layout-item>
     </rapid-layout-region>
  </rapid-layout>
`;
