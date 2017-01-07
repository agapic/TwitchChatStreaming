import React from 'react';
import { FormattedMessage } from 'react-intl';


import Wrapper from './Wrapper';
import messages from './messages';

function Footer() {
  return (
    <Wrapper>
      <section>
        <FormattedMessage {...messages.licenseMessage} />
      </section>
      <section>

      </section>
      <section>
        <FormattedMessage
          {...messages.authorMessage}
          values={{
            author: 'Andrew Gapic',
          }}
        />
      </section>
    </Wrapper>
  );
}

export default Footer;
