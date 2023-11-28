import { Fragment } from 'react'
import {
    Button,
    Divider,
    Input,
    Option,
    Select,
    Sheet,
    Slider,
    Stack,
    Typography
} from '@mui/joy'
import {
    PLAYFIELD_STYLE,
    PLAYFIELD_SVG_VIEWBOX,
    PLAYFIELD_SVG_WIDTH,
    PLAYFIELD_SVG_HEIGHT,
    PLAYFIELD_TEXT_STYLE,
    PLAYER_HEIGHT,
    PLAYER_SPEED,
} from '../utils/const'
import usePlayField from './hooks/usePlayField'
import PlayFieldPlayer from './PlayFieldPlayer'

export default function PlayField() {

    const {
        gameId,
        playerId,
        disableEdit,
        teamsScore,
        playerDTOMap,
        lineProps,
        ballProps,
        handleKeyDown,
        handleKeyUp,
        handleAnimationEnd,
        handleGameIdChange,
        handlePlayerIdChange,
        handleButtonClick,
        handleTeamChange,
        handleChangeHeight,
        handleChangeSpeed,
        handleSendSettings
    } = usePlayField()
    const handleChange = (
        event: React.SyntheticEvent | null,
        newValue: string | null,
    ) => {
        handleTeamChange(newValue)
    }
    return (
        <Fragment>
            <div style={PLAYFIELD_STYLE} hidden= {!disableEdit}>
                {teamsScore && (
                    <Typography level='h1' style={PLAYFIELD_TEXT_STYLE}>
                        {`${teamsScore.leftTeamScore}-${teamsScore.rightTeamScore}`}
                    </Typography>
                )}
                <svg
                    tabIndex={0}
                    overflow='visible'
                    viewBox={PLAYFIELD_SVG_VIEWBOX}
                    width={PLAYFIELD_SVG_WIDTH}
                    height={PLAYFIELD_SVG_HEIGHT}
                    onKeyDown={handleKeyDown}
                    onKeyUp={handleKeyUp}
                >
                    <circle
                        {...ballProps}
                        onAnimationEnd={handleAnimationEnd}
                    />
                    <line {...lineProps} />
                    {Object.keys(playerDTOMap).map((playerDTOId) => (
                        <PlayFieldPlayer
                            key={playerDTOId}
                            userId={playerId}
                            playerId={playerDTOId}
                            player={playerDTOMap[playerDTOId]}
                        />
                    ))}
                </svg>
            </div>
            <Stack direction={'column'} spacing={2} divider={disableEdit?<Divider orientation='horizontal'><Typography color='primary' level='h2' fontFamily={'serif'}>IMPOSTAZIONI</Typography></Divider>:''}>
                <Stack
                    direction='row'
                    spacing={2}
                >
                    <Input
                        size='sm'
                        variant='soft'
                        placeholder="Game ID"
                        disabled={disableEdit}
                        onChange={handleGameIdChange}
                        value={gameId}
                    />
                    <Input
                        size='sm'
                        variant='soft'
                        placeholder="Player ID"
                        disabled={disableEdit}
                        onChange={handlePlayerIdChange}
                        value={playerId}
                    />
                    <Select placeholder="Cambia squadra" disabled={!disableEdit} onChange={handleChange} >
                        <Option value="left">Left</Option>
                        <Option value="right">Right</Option>
                    </Select>

                    <Button
                        size='sm'
                        disabled={disableEdit}
                        onClick={handleButtonClick}
                    >
                        connect
                    </Button>
                    <br></br>
                    <Sheet variant="soft" color="primary" invertedColors={true} sx={{p:1}}>
                        {'gameId = ' + localStorage.getItem('gameId')+' '}
                        {'playerId = ' + localStorage.getItem('playerId')}
                    </Sheet>

                </Stack>
                {disableEdit?<Stack direction={'column'} spacing={2} alignItems={'flex-start'} justifyContent={'space-around'}>
                    <Typography color='primary' level='h2' fontFamily={'serif'} >Player Size</Typography>
                    <Slider
                        defaultValue={PLAYER_HEIGHT}
                        onChange={handleChangeHeight}
                        disabled={false}
                        marks={true}
                        valueLabelDisplay="on"
                        variant="solid"
                    />
                    <Typography color='primary' level='h2' fontFamily={'serif'}>Player Speed</Typography>
                    <Slider
                        defaultValue={PLAYER_SPEED}
                        onChange={handleChangeSpeed}
                        disabled={false}
                        min={60}
                        max={500}
                        marks={true}
                        valueLabelDisplay="on"
                        variant="solid"
                    />
                    <Button onClick={handleSendSettings}>Conferma Impostazioni</Button>
                </Stack>:<br></br> }
            </Stack>
        </Fragment>
    )
}
