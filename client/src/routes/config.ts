import { getUser, navigateTo } from '@genesislcap/foundation-auth';
import { Connect } from '@genesislcap/foundation-comms';
import { FoundationRouterConfiguration } from '@genesislcap/foundation-ui';
import { GENESIS_SOCKET_URL, PUBLIC_PATH } from '@genesislcap/foundation-utils';
import { defaultLayout, loginLayout } from '../layouts';
import { ExchangeData } from './exchange-data/exchange-data';
import { NotFound } from './not-found/not-found';
import { defaultNotPermittedRoute, NotPermitted } from './not-permitted/not-permitted';
import { LoginSettings } from './types';

// eslint-disable-next-line
declare var ENABLE_SSO: string;

const ssoSettings =
  typeof ENABLE_SSO !== 'undefined' && ENABLE_SSO === 'true'
    ? {
        autoAuth: true,
        sso: {
          toggled: true,
          identityProvidersPath: 'gwf/sso/list',
        },
      }
    : {};

const publicPath = typeof PUBLIC_PATH !== 'undefined' ? PUBLIC_PATH : '';

export class MainRouterConfig extends FoundationRouterConfiguration<LoginSettings> {
  @Connect private connect: Connect;

  async configure() {
    this.configureAnalytics();
    this.configureRoutePermittedChecks();
    this.configureFallbackRouteDefinition();
    this.title = 'Howto Csv Ingress';
    this.defaultLayout = defaultLayout;

    const authPath = 'login';

    this.routes.map(
      { path: '', redirect: authPath },
      {
        path: authPath,
        name: 'login',
        title: 'Login',
        element: async () => {
          const { configure, defaultAuthConfig } = await import(
            '@genesislcap/foundation-auth/config'
          );
          return configure({
            omitRoutes: ['request-account', 'forgot-password'],
            hostPath: this.loginPath,
            // The router accepts the WebSocket only once the HTTP login has set its session cookie
            postLoginRedirect: async () => {
              await this.connect.connect(GENESIS_SOCKET_URL);
              const lastPath =
                getUser().lastPath() === `${publicPath}/` ? undefined : getUser().lastPath();
              navigateTo(lastPath ?? `${publicPath}/exchange-data`);
            },
            postLogoutRedirect: () => {
              if (this.connect.isConnected) {
                this.connect.disconnect();
              }
              defaultAuthConfig.postLogoutRedirect();
            },
            ...ssoSettings,
          });
        },
        layout: loginLayout,
        settings: { public: true },
        childRouters: true,
      },
      { path: 'not-found', element: NotFound, title: 'Not Found', name: 'not-found' },
      {
        path: defaultNotPermittedRoute,
        element: NotPermitted,
        title: 'Not Permitted',
        name: defaultNotPermittedRoute,
      },
      {
        path: 'exchange-data',
        element: ExchangeData,
        title: 'Exchange data',
        name: 'exchange-data',
        navItems: [
          {
            title: 'Exchange data',
            icon: {
              name: 'cog',
              variant: 'solid',
            },
            permission: '',
          },
        ],
      },
    );

    this.contributors.push({
      navigate: async (phase) => {
        const settings = phase.route.settings;

        /**
         * If the route is public or the user is authenticated don't block
         */
        if (settings?.public || this.user.isAuthenticated) {
          return;
        }

        /**
         * Otherwise route them to login
         */
        this.navigationPhaseLoginRedirect(phase);
      },
    });
  }
}
