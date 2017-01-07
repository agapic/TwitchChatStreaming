import React from 'react';
import Img from './Img';
import H1 from 'components/H1';
import Banner from './banner.jpg';


class Header extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    return (
      <div>
        <Img src={Banner} alt="Logo" />
        <H1> Twitch Real-time Stats </H1>
      </div>
    );
  }
}

export default Header;
