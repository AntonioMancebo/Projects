AppSettingsPage({
  state: { props: {} },

  save(key, value) {
    this.state.props.settingsStorage.setItem(key, String(value || '').trim())
  },

  build(props) {
    this.state.props = props
    const bridgeUrl = props.settingsStorage.getItem('bridgeUrl') || ''
    const bridgeToken = props.settingsStorage.getItem('bridgeToken') || ''
    const scope = props.settingsStorage.getItem('scope') || 'today'

    return View(
      {
        style: {
          padding: '18px 20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '14px'
        }
      },
      [
        Text({
          text: 'SP Watch',
          style: { fontSize: '22px', fontWeight: 'bold' }
        }),
        Text({
          text: 'Conecta el reloj con el bridge local de Super Productivity.',
          style: { fontSize: '13px', color: '#666' }
        }),
        TextInput({
          label: 'Bridge URL',
          value: bridgeUrl,
          placeholder: 'http://192.168.1.50:8787',
          onChange: (value) => this.save('bridgeUrl', value)
        }),
        TextInput({
          label: 'Token del bridge',
          value: bridgeToken,
          placeholder: 'token compartido',
          onChange: (value) => this.save('bridgeToken', value)
        }),
        TextInput({
          label: 'Lista',
          value: scope,
          placeholder: 'today o all',
          onChange: (value) => this.save('scope', value === 'all' ? 'all' : 'today')
        }),
        Text({
          text: 'En el reloj: toque ✓ = completar · pulsación larga ✓ = iniciar tarea',
          style: { fontSize: '12px', color: '#777' }
        })
      ]
    )
  }
})
