/**
 * RepoListItem
 *
 * Lists the name and the issue count of a repository
 */

import React from 'react';
import { FormattedNumber } from 'react-intl';
import ListItem from 'components/ListItem';
import Link from './Link';
import Wrapper from './Wrapper';

export class WordCountListItem extends React.PureComponent { // eslint-disable-line react/prefer-stateless-function
  render() {
    const item = this.props.item;

    // Put together the content of the word counts
    const content = (
      <Wrapper>
        <Link href={`http://lmgtfy.com/?q=${item.word}`} target="_blank">
          {item.word}
        </Link>
        <FormattedNumber value={item.count} />
      </Wrapper>
    );

    // Render the content into a list item
    return (
      <ListItem key={`word-count-list-item-${item.word}`} item={content} />
    );
  }
}

WordCountListItem.propTypes = {
  item: React.PropTypes.object,
};

export default WordCountListItem;
