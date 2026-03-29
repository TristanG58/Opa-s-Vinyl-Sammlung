package com.opasvinyl.sammlung.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opasvinyl.sammlung.data.local.entity.RecordCondition
import com.opasvinyl.sammlung.ui.theme.ConditionFair
import com.opasvinyl.sammlung.ui.theme.ConditionGood
import com.opasvinyl.sammlung.ui.theme.ConditionMint
import com.opasvinyl.sammlung.ui.theme.ConditionNearMint
import com.opasvinyl.sammlung.ui.theme.ConditionPoor
import com.opasvinyl.sammlung.ui.theme.ConditionVeryGood
import com.opasvinyl.sammlung.ui.theme.VinylBrownLight

@Composable
fun ConditionBadge(
    condition: RecordCondition,
    modifier: Modifier = Modifier
) {
    val color = when (condition) {
        RecordCondition.MINT -> ConditionMint
        RecordCondition.NEAR_MINT -> ConditionNearMint
        RecordCondition.VERY_GOOD_PLUS, RecordCondition.VERY_GOOD -> ConditionVeryGood
        RecordCondition.GOOD_PLUS, RecordCondition.GOOD -> ConditionGood
        RecordCondition.FAIR -> ConditionFair
        RecordCondition.POOR -> ConditionPoor
        RecordCondition.NOT_GRADED -> VinylBrownLight
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = condition.label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}
