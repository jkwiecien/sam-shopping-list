## Rules for ui generation
When generating UI stick to the rules below:
- Map material composables to custom ones in the `custom composables mapping` section
- Always add imports to the known classes and functions
- Always use imports so full classpath is not required in implementation
- Always encapsulate preview in `pl.techbrewery.sam.ui.theme.SAMTheme {}`
- When creating spacers or paddings, use only values from object `pl.techbrewery.sam.ui.shared.Spacing`. Reference to them instead of hardcode dp. Stick strictly to values in the Spacing object.
- When creating screen root composable, start from `Surface` composable with `modifier = Modifier.fillMaxSize()`
- Stick to the colors specified in SAMTheme.colorScheme. Don't create new colors.
- instead of using `Spacer(Spacing.Small)` use `pl.techbrewery.sam.ui.shared.SmallSpacingBox()` and other sizes respectively

## Custom composables mapping
- `Button` -> `pl.techbrewery.sam.ui.shared.PrimaryFilledButton`
- `OutlinedButton` -> `pl.techbrewery.sam.ui.shared.PrimaryOutlinedButton`
- `TextField` -> `pl.techbrewery.sam.ui.shared.PrimaryTextField`
