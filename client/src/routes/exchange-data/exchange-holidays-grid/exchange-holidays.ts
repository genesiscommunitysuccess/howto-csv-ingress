import { User } from '@genesislcap/foundation-user';
import { customElement, GenesisElement } from '@genesislcap/web-core';
import { ExchangeHolidaysStyles as styles } from './exchange-holidays.styles';
import { ExchangeHolidaysTemplate as template } from './exchange-holidays.template';

@customElement({
  name: 'exchange-data-exchange-holidays-grid',
  template,
  styles,
})
export class ExchangeDataExchangeHolidaysGrid extends GenesisElement {
  @User user: User;
}
