import { User } from '@genesislcap/foundation-user';
import { customElement, GenesisElement } from '@genesislcap/web-core';
import { ExchangeDataStyles as styles } from './exchange-data.styles';
import { ExchangeDataTemplate as template } from './exchange-data.template';

@customElement({
  name: 'exchange-data-route',
  template,
  styles,
})
export class ExchangeData extends GenesisElement {
  @User user: User;

  constructor() {
    super();
  }
}
