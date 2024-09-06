import { html, whenElse, repeat } from '@genesislcap/web-core';
import { getViewUpdateRightComponent } from '../../../utils';
import type { ExchangeDataExchangeHolidaysGrid } from './exchange-holidays';
import { gridOptions } from './exchange-holidays.gridOptions';


export const ExchangeHolidaysTemplate = html<ExchangeDataExchangeHolidaysGrid>`
    ${whenElse(
        (x) => getViewUpdateRightComponent(x.user, 'ExchangeView'),
        html`
          <rapid-grid-pro
            header-case-type="capitalCase"
            only-template-col-defs
            enable-row-flashing
            enable-cell-flashing
            >
            <grid-pro-server-side-datasource
              resource-name="EXCHANGE_HOLIDAY_VIEW"
              :deferredGridOptions=${() => ({ onRowClicked: gridOptions?.onRowClicked })}
            ></grid-pro-server-side-datasource>
            ${repeat(
              (x) => gridOptions?.columnDefs,
              html`
                <grid-pro-column :definition=${(x) => x}></grid-pro-column>
              `,
            )}
          </rapid-grid-pro>
        `,
        html`
            <not-permitted-component></not-permitted-component>
        `,
    )}`;
