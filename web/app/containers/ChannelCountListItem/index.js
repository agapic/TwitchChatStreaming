/**
 * ChannelCountListItem
 *
 * Lists the name, game, and message count of a channel.
 */

import React from 'react';
import { FormattedNumber } from 'react-intl';

import ListItem from 'components/ListItem';
import ChannelLink from './ChannelLink';
import ChannelWrapper from './ChannelWrapper';

export class ChannelCountListItem extends React.PureComponent { // eslint-disable-line react/prefer-stateless-function
  render() {
    const item = this.props.item;

    // Put together the content of the repository
    const content = (
      <ChannelWrapper>
        <ChannelLink href={`https://www.twitch.tv/${item.name}`} target="_blank">
          {item.name} ({item.game})
        </ChannelLink>
        <FormattedNumber value={item.message_count} />
      </ChannelWrapper>
    );

    // Render the content into a list item
    return (
      <ListItem key={`channel-count-list-item-${item.name}`} item={content} />
    );
  }
}

ChannelCountListItem.propTypes = {
  item: React.PropTypes.object,
};

export default ChannelCountListItem;
